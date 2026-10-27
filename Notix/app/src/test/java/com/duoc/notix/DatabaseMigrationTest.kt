package com.duoc.notix

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.LocalAuthRepository
import com.duoc.notix.data.LocalProcedureRepository
import com.duoc.notix.data.PasswordHasher
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.domain.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class DatabaseMigrationTest {
    @Test fun migrationKeepsOldAccountsDraftsAndAddsHistory() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val path = context.getDatabasePath(name)
        path.parentFile!!.mkdirs()
        val old = SQLiteDatabase.openOrCreateDatabase(path, null)
        val schema = JSONObject(File("schemas/com.duoc.notix.data.local.NotixDatabase/1.json").readText()).getJSONObject("database")
        val marker = "$" + "{TABLE_NAME}"
        val entities = schema.getJSONArray("entities")
        for (index in 0 until entities.length()) {
            val entity = entities.getJSONObject(index)
            old.execSQL(entity.getString("createSql").replace(marker, entity.getString("tableName")))
            val indices = entity.getJSONArray("indices")
            for (j in 0 until indices.length()) old.execSQL(indices.getJSONObject(j).getString("createSql").replace(marker, entity.getString("tableName")))
        }
        old.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        old.execSQL("INSERT INTO room_master_table (id, identity_hash) VALUES (42, ?)", arrayOf(schema.getString("identityHash")))
        val hasher = PasswordHasher()
        val salt = hasher.newSalt()
        old.insertOrThrow("users", null, ContentValues().apply {
            put("id", 1L); put("displayName", "Persona Anterior"); put("email", "anterior@example.com")
            put("passwordSalt", salt); put("passwordHash", hasher.hash("prueba123", salt))
        })
        old.execSQL("INSERT INTO procedures (id,userId,title,createdAt) VALUES (1,1,'Borrador anterior',1234)")
        old.version = 1
        old.close()
        val database = Room.databaseBuilder(context, NotixDatabase::class.java, name)
            .addMigrations(NotixDatabase.MIGRATION_1_2).build()
        try {
            val account = (LocalAuthRepository(database.userDao()).login("anterior@example.com", "prueba123") as AuthResult.Success).user
            assertEquals(AccountType.PERSON, account.type)
            val procedure = LocalProcedureRepository(database).observe(account).first().single()
            assertEquals("Borrador anterior", procedure.title)
            assertEquals(ProcedureStatus.DRAFT, procedure.status)
            assertEquals("Borrador anterior conservado.", procedure.history.single().note)
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
