package com.tuusuario.autoclicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnPermission: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPermission = findViewById(R.id.btnGiantPermission)

        // Al presionar el botón gigante, validamos los permisos paso a paso
        btnPermission.setOnClickListener {
            checkAndRequestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        // Cada vez que el usuario regresa a la app, verificamos si ya cumplió
        if (hasAllPermissions()) {
            btnPermission.text = "¡PERMISOS CONCEDIDOS!\nINICIAR SERVICIO"
            btnPermission.setBackgroundColor(0xFF00CC66.toInt()) // Verde
            
            // Iniciamos el menú flotante
            startService(Intent(this, FloatingWindowService::class.java))
            finish() // Cerramos la pantalla principal para que juegue
        } else {
            btnPermission.text = "⚠️ OBLIGATORIO ⚠️\nCONCEDER PERMISOS PARA USAR"
            btnPermission.setBackgroundColor(0xFFFF3333.toInt()) // Rojo
        }
    }

    private fun hasAllPermissions(): Boolean {
        // 1. Verificar permiso de superposición (dibujar encima de Roblox)
        val overlayOK = Settings.canDrawOverlays(this)
        // 2. Verificar servicio de accesibilidad (para hacer clics automáticos)
        val accessibilityOK = isAccessibilityServiceEnabled()
        return overlayOK && accessibilityOK
    }

    private fun checkAndRequestPermissions() {
        // Paso 1: Obligar a dar permiso de dibujar sobre otras apps
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Activa 'Mostrar sobre otras aplicaciones'", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }

        // Paso 2: Obligar a activar el Servicio de Accesibilidad
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "Busca 'AutoClicker' en Apps Descargadas y actívalo", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "$packageName/${MyAccessibilityService::class.java.canonicalName}"
        val enabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED, 0
        )
        if (enabled == 1) {
            val settingValue = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return settingValue?.contains(service) == true
        }
        return false
    }
}

