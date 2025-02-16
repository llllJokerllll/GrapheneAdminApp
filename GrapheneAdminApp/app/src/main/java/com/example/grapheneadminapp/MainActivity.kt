package com.example.grapheneadminapp

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.widget.Toast
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.security.SecureRandom
import java.security.spec.PBKDF2WithHmacSHA512KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import java.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.util.Properties
import javax.mail.*
import javax.mail.internet.*

// Clase para recibir eventos del administrador de dispositivos
class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Administrador de dispositivo activado", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Administrador de dispositivo desactivado", Toast.LENGTH_SHORT).show()
    }
}

// Clase principal de la aplicación
class MainActivity : AppCompatActivity() {
    private lateinit var adminHelper: DeviceAdminHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Detecta debugging y cierra la app si es detectado
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            throw RuntimeException("Debugging detectado. Cerrando aplicación.")
        }

        adminHelper = DeviceAdminHelper(this)

        // Mostrar pantalla de autenticación antes de aplicar restricciones
        adminHelper.requestPassword {
            adminHelper.restrictAppInstallation()
            adminHelper.restrictNetworkChanges()
        }
    }
}

// Clase para gestionar las funciones del administrador de dispositivos
class DeviceAdminHelper(private val context: Context) {
    private val dpm: DevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, AdminReceiver::class.java)
    private val salt = SecureRandom().generateSeed(16) // Genera un salt aleatorio
    private val hashedPassword = hashPassword("Prueba123..*") // Contraseña hasheada
    private val encryptedEmail = encryptData("Prueba15@proton.me") // Correo cifrado
    private val encryptedSenderEmail = encryptData("Prueba15@proton.me") // Correo del remitente cifrado
    private val encryptedSenderPassword = encryptData("Prueba99..*") // Contraseña del remitente cifrada
    private var failedAttempts = 0 // Contador de intentos fallidos

    // Función para hashear la contraseña usando PBKDF2WithHmacSHA512
    private fun hashPassword(password: String): String {
        val iterations = 200000
        val keyLength = 512
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        val spec = PBKDF2WithHmacSHA512KeySpec(password.toCharArray(), salt, iterations, keyLength)
        val key = factory.generateSecret(spec)
        return Base64.getEncoder().encodeToString(key.encoded)
    }

    // Función para cifrar datos usando AES/GCM/NoPadding
    private fun encryptData(data: String): String {
        val key = SecretKeySpec(salt, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8)))
    }

    // Función para descifrar datos usando AES/GCM/NoPadding
    private fun decryptData(data: String): String {
        val key = SecretKeySpec(salt, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return String(cipher.doFinal(Base64.getDecoder().decode(data)), StandardCharsets.UTF_8)
    }

    // Función para restringir la instalación y desinstalación de aplicaciones
    fun restrictAppInstallation() {
        if (dpm.isAdminActive(adminComponent)) {
            dpm.addUserRestriction(adminComponent, DevicePolicyManager.DISALLOW_INSTALL_APPS)
            dpm.addUserRestriction(adminComponent, DevicePolicyManager.DISALLOW_UNINSTALL_APPS)
            Toast.makeText(context, "Instalación y desinstalación de apps bloqueada", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para restringir los cambios en la configuración de red
    fun restrictNetworkChanges() {
        if (dpm.isAdminActive(adminComponent)) {
            dpm.addUserRestriction(adminComponent, DevicePolicyManager.DISALLOW_CONFIG_WIFI)
            dpm.addUserRestriction(adminComponent, DevicePolicyManager.DISALLOW_CONFIG_MOBILE_NETWORKS)
            Toast.makeText(context, "Modificación de redes bloqueada", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para solicitar la contraseña al usuario
    fun requestPassword(callback: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.password_dialog, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.password_input)

        builder.setView(dialogView)
        builder.setPositiveButton("OK") { _, _ ->
            if (hashPassword(passwordInput.text.toString()) == hashedPassword) {
                failedAttempts = 0
                callback()
            } else {
                failedAttempts++
                Toast.makeText(context, "Clave incorrecta", Toast.LENGTH_SHORT).show()
                if (failedAttempts >= 3) {
                    sendSecurityAlert()
                    wipeDevice()
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    // Función para enviar una alerta por correo electrónico
    private fun sendSecurityAlert() {
        val recipient = decryptData(encryptedEmail)
        val senderEmail = decryptData(encryptedSenderEmail)
        val senderPassword = decryptData(encryptedSenderPassword)

        val properties = Properties()
        properties["mail.transport.protocol"] = "smtp"
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.starttls.enable"] = "true"
        properties["mail.smtp.host"] = "smtp.protonmail.com"
        properties["mail.smtp.port"] = "587"
        properties["mail.smtp.ssl.trust"] = "smtp.protonmail.com"

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, senderPassword)
            }
        })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(senderEmail))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
            message.subject = "Intento de acceso fallido"
            message.setText("Se ha detectado un intento de acceso fallido en tu dispositivo.")
            Transport.send(message)
        } catch (e: MessagingException) {
            Log.e("SecurityAlert", "Error al enviar el correo", e)
        }
    }

    private fun wipeDevice() {
        if (dpm.isAdminActive(adminComponent)) {
            dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE)
            Toast.makeText(context, "Dispositivo restablecido de fábrica", Toast.LENGTH_LONG).show()
        }
    }
}
