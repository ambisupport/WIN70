package com.example.wn70aremote

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

data class Key(val id: String, val label: String, val bg: Int = 0xFF14161C.toInt(), val fg: Int = Color.WHITE, val weight: Float = 1f)

class MainActivity : Activity() {

    private var ir: ConsumerIrManager? = null
    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private val keyMap = HashMap<String, Key>()

    private val order = listOf(
        "POWER","FREEZE","1","2","3","4","5","6","7","8","9","DASH","0","QVIEW",
        "VOLUP","ECO","CHUP","VOLDN","MUTE","CHDN","GOTO","SUBTITLE","ASPECT",
        "FAV","UP","MENU","LEFT","OK","RIGHT","INPUT","DOWN","EXIT",
        "SMODE","PMODE","INFO","ROOT","PREV","NEXT","REW","FF","STOP","FAVMINUS","FAVPLUS","PLAY"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("codes", Context.MODE_PRIVATE)
        ir = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        window.statusBarColor = 0xFF0B0D12.toInt()
        buildUi()
        if (ir?.hasIrEmitter() != true) {
            AlertDialog.Builder(this)
                .setTitle("No IR blaster found")
                .setMessage("This phone has no infrared emitter, so it cannot control a TV. Use a phone with an IR blaster (many Xiaomi/Redmi/Poco models) or an external USB/audio-jack IR blaster.")
                .setPositiveButton("OK", null).show()
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun k(id: String, label: String = id, bg: Int = 0xFF14161C.toInt(), fg: Int = Color.WHITE, w: Float = 1f): Key =
        Key(id, label, bg, fg, w).also { keyMap[id] = it }

    private fun buildUi() {
        val root = ScrollView(this).apply { setBackgroundColor(0xFF0B0D12.toInt()) }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(24))
        }
        root.addView(col)

        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply {
            text = "WN-70A  •  Universal LED TV Remote"
            setTextColor(0xFFB8C0D4.toInt()); textSize = 14f; typeface = Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(Button(this).apply {
            text = "⚙"; setOnClickListener { showSettings() }
        })
        col.addView(header)

        val red = 0xFFD81B3C.toInt()
        row(col, k("POWER", "⏻", red, Color.WHITE, 1f), spacer(2f), k("FREEZE", "FREEZE", w = 1f))
        row(col, k("1"), k("2"), k("3"))
        row(col, k("4"), k("5"), k("6"))
        row(col, k("7"), k("8"), k("9"))
        row(col, k("DASH", "-/--"), k("0"), k("QVIEW", "Q.VIEW"))

        val mid = LinearLayout(this)
        mid.addView(tall(k("VOLUP", "+"), k("VOLDN", "−"), "VOL"), lp(1f))
        val center = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        center.addView(btn(k("ECO")), lpH(1f)); center.addView(btn(k("MUTE")), lpH(1f))
        mid.addView(center, lp(1f))
        mid.addView(tall(k("CHUP", "+"), k("CHDN", "−"), "P"), lp(1f))
        col.addView(mid, LinearLayout.LayoutParams(-1, dp(170)))

        row(col, k("GOTO"), k("SUBTITLE", "SubTitle"), k("ASPECT", "Aspect"))
        row(col, k("FAV"), k("UP", "P∧"), k("MENU"))
        row(col, k("LEFT", "<"), k("OK"), k("RIGHT", ">"))
        row(col, k("INPUT"), k("DOWN", "P∨"), k("EXIT"))
        row(col,
            k("SMODE", "S.MODE", 0xFFC62828.toInt()), k("PMODE", "P.MODE", 0xFF00796B.toInt()),
            k("INFO", "INFO", 0xFFF9D71C.toInt(), Color.BLACK), k("ROOT", "ROOT", 0xFF1E5BD8.toInt()))
        row(col, k("PREV", "|◀◀"), k("NEXT", "▶▶|"), k("REW", "◀◀"), k("FF", "▶▶"))
        row(col, k("STOP", "■"), k("FAVMINUS", "FAV−"), k("FAVPLUS", "FAV+"), k("PLAY", "▶❚❚"))

        col.addView(TextView(this).apply {
            text = "Tap = send  •  Long-press = edit code\nFREEZE long-press = edit service-menu sequence"
            setTextColor(0xFF6B748A.toInt()); textSize = 11f; gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        })
        setContentView(root)
    }

    private fun lp(w: Float) = LinearLayout.LayoutParams(0, -1, w).apply { setMargins(dp(3), dp(3), dp(3), dp(3)) }
    private fun lpH(w: Float) = LinearLayout.LayoutParams(-1, 0, w).apply { setMargins(dp(3), dp(3), dp(3), dp(3)) }
    private fun spacer(w: Float): Key = Key("", "", weight = w)

    private fun row(parent: LinearLayout, vararg keys: Key) {
        val r = LinearLayout(this)
        for (key in keys) {
            if (key.id.isEmpty()) { r.addView(View(this), LinearLayout.LayoutParams(0, 1, key.weight)); continue }
            r.addView(btn(key), LinearLayout.LayoutParams(0, dp(52), key.weight).apply { setMargins(dp(3), dp(3), dp(3), dp(3)) })
        }
        parent.addView(r)
    }

    private fun tall(top: Key, bottom: Key, caption: String): LinearLayout {
        val c = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        c.addView(btn(top, 26f), lpH(1f))
        c.addView(TextView(this).apply { text = caption; setTextColor(Color.WHITE); gravity = Gravity.CENTER; textSize = 12f },
            LinearLayout.LayoutParams(-1, dp(20)))
        c.addView(btn(bottom, 26f), lpH(1f))
        return c
    }

    private fun btn(key: Key, size: Float = 14f): Button = Button(this).apply {
        text = key.label; textSize = size; setTextColor(key.fg); isAllCaps = false
        background = GradientDrawable().apply { setColor(key.bg); cornerRadius = dp(10).toFloat(); setStroke(dp(1), 0xFF2A2E3A.toInt()) }
        setOnClickListener { buzz(); if (key.id == "FREEZE") onFreeze() else sendKey(key.id) }
        setOnLongClickListener { if (key.id == "FREEZE") editMacro() else editCode(key.id); true }
    }

    private fun buzz() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (android.os.Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(20, 120)) else @Suppress("DEPRECATION") v.vibrate(20)
    }

    private fun globalAddr() = prefs.getInt("global_addr", 0x00)

    private fun codeOf(id: String): Pair<Int, Int> {
        val s = prefs.getString("code_$id", null)
        if (s != null) { val p = s.split(":"); return p[0].toInt(16) to p[1].toInt(16) }
        return globalAddr() to order.indexOf(id).coerceAtLeast(0)
    }

    private fun sendKey(id: String) {
        val (a, c) = codeOf(id)
        sendNec(a, c)
    }

    private fun sendNec(addr: Int, cmd: Int) {
        val mgr = ir
        if (mgr == null || !mgr.hasIrEmitter()) { toast("No IR blaster"); return }
        val data: Long = if (addr > 0xFF)
            (addr and 0xFFFF).toLong() or ((cmd and 0xFF).toLong() shl 16) or ((cmd.inv() and 0xFF).toLong() shl 24)
        else
            (addr and 0xFF).toLong() or ((addr.inv() and 0xFF).toLong() shl 8) or
                ((cmd and 0xFF).toLong() shl 16) or ((cmd.inv() and 0xFF).toLong() shl 24)
        val p = ArrayList<Int>(); p.add(9000); p.add(4500)
        for (i in 0 until 32) { p.add(560); p.add(if (((data shr i) and 1L) == 1L) 1690 else 560) }
        p.add(560)
        try { mgr.transmit(38000, p.toIntArray()) } catch (e: Exception) { toast("IR error: ${e.message}") }
    }

    private fun macro() = prefs.getString("freeze_macro", "MENU,1,1,4,7") ?: ""
    private fun macroDelay() = prefs.getInt("macro_delay", 400)

    private fun onFreeze() {
        val seq = macro().split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }
        if (seq.isEmpty()) { sendKey("FREEZE"); return }
        toast("Opening service menu…")
        seq.forEachIndexed { i, token ->
            handler.postDelayed({
                if (keyMap.containsKey(token)) sendKey(token)
                else if (token.startsWith("RAW=")) {
                    val p = token.removePrefix("RAW=").split(":")
                    if (p.size == 2) sendNec(p[0].toInt(16), p[1].toInt(16))
                }
            }, i * macroDelay().toLong())
        }
    }

    private fun editMacro() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0) }
        val seq = EditText(this).apply { setText(macro()); hint = "MENU,1,1,4,7" }
        val delay = EditText(this).apply { setText(macroDelay().toString()); inputType = InputType.TYPE_CLASS_NUMBER; hint = "delay ms" }
        box.addView(TextView(this).apply { text = "Key sequence sent when FREEZE is tapped (comma separated).\nKeys: 0-9, MENU, OK, INPUT, ROOT, INFO, EXIT, UP, DOWN, LEFT, RIGHT, SMODE, PMODE …\nCustom code: RAW=addr:cmd (hex)\nLeave empty to send the plain FREEZE code." })
        box.addView(seq)
        box.addView(TextView(this).apply { text = "Delay between keys (ms)" }); box.addView(delay)
        AlertDialog.Builder(this).setTitle("FREEZE → Service menu").setView(box)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit().putString("freeze_macro", seq.text.toString())
                    .putInt("macro_delay", delay.text.toString().toIntOrNull() ?: 400).apply()
            }
            .setNeutralButton("Test") { _, _ ->
                prefs.edit().putString("freeze_macro", seq.text.toString())
                    .putInt("macro_delay", delay.text.toString().toIntOrNull() ?: 400).apply(); onFreeze()
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun editCode(id: String) {
        val (a, c) = codeOf(id)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0) }
        val addr = EditText(this).apply { setText("%02X".format(a)); hint = "address (hex)" }
        val cmd = EditText(this).apply { setText("%02X".format(c)); hint = "command (hex)" }
        box.addView(TextView(this).apply { text = "NEC address (hex)" }); box.addView(addr)
        box.addView(TextView(this).apply { text = "NEC command (hex)" }); box.addView(cmd)
        fun parse() = (addr.text.toString().toIntOrNull(16) ?: 0) to (cmd.text.toString().toIntOrNull(16) ?: 0)
        AlertDialog.Builder(this).setTitle("Edit key: $id").setView(box)
            .setPositiveButton("Save") { _, _ -> val (x, y) = parse(); prefs.edit().putString("code_$id", "%X:%X".format(x, y)).apply() }
            .setNeutralButton("Test") { _, _ -> val (x, y) = parse(); sendNec(x, y) }
            .setNegativeButton("Cancel", null).show()
    }

    private fun showSettings() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0) }
        val addr = EditText(this).apply { setText("%X".format(globalAddr())) }
        box.addView(TextView(this).apply { text = "Default NEC address for all keys (hex). Keys you never edited use this address with placeholder commands." })
        box.addView(addr)
        AlertDialog.Builder(this).setTitle("Settings").setView(box)
            .setPositiveButton("Save") { _, _ -> prefs.edit().putInt("global_addr", addr.text.toString().toIntOrNull(16) ?: 0).apply() }
            .setNeutralButton("Reset all codes") { _, _ -> prefs.edit().clear().apply(); toast("Reset") }
            .setNegativeButton("Cancel", null).show()
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
