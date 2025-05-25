package com.seongpark.bbkeyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class MyInputMethodService : InputMethodService() {

    private lateinit var keyboardMain: LinearLayout
    private lateinit var numberPopup1: LinearLayout
    private lateinit var numberPopup2: LinearLayout
    private lateinit var specialCharPopup: LinearLayout
    private lateinit var btnShift: Button

    private var isShiftOn = false
    private var isShiftLocked = false

    private val shiftHoldHandler = Handler()
    private var isShiftPressed = false

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.input_view, null)

        keyboardMain = view.findViewById(R.id.keyboard_main)
        numberPopup1 = view.findViewById(R.id.number_popup)
        numberPopup2 = view.findViewById(R.id.number_popup_2)
        specialCharPopup = view.findViewById(R.id.special_char_popup)

        btnShift = view.findViewById(R.id.btn_shift)
        val btnSpecialToggle = view.findViewById<Button>(R.id.btn_special_toggle)
        val btnAt = view.findViewById<Button>(R.id.btn_at)
        val btnQuestion = view.findViewById<Button>(R.id.btn_question)
        val btnExclaim = view.findViewById<Button>(R.id.btn_exclaim)
        val btnNum = view.findViewById<Button>(R.id.btn_num)

        btnShift.setOnClickListener {
            if (!isShiftLocked) {
                isShiftOn = !isShiftOn
                updateShiftState()
            }
        }

        btnShift.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isShiftPressed = true
                    shiftHoldHandler.postDelayed({
                        if (isShiftPressed) {
                            isShiftLocked = !isShiftLocked
                            isShiftOn = isShiftLocked
                            updateShiftState()
                        }
                    }, 1000)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isShiftPressed = false
                }
            }
            false
        }

        btnSpecialToggle.setOnClickListener { toggleSpecialCharPad() }
        btnAt.setOnClickListener { commitText("?") }
        btnQuestion.setOnClickListener { commitText(",") }
        btnExclaim.setOnClickListener { commitText(".") }
        btnNum.setOnClickListener { toggleNumberPad() }

        listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        ).forEach { id ->
            view.findViewById<Button>(id).setOnClickListener {
                commitText((it as Button).text.toString())
            }
        }

        listOf(
            R.id.btn_special_0, R.id.btn_special_1, R.id.btn_special_2,
            R.id.btn_special_3, R.id.btn_special_4, R.id.btn_special_5,
            R.id.btn_special_6, R.id.btn_special_7, R.id.btn_special_8,
            R.id.btn_special_9, R.id.btn_special_10, R.id.btn_special_11,
            R.id.btn_special_12, R.id.btn_special_13, R.id.btn_special_14,
            R.id.btn_special_15, R.id.btn_special_16, R.id.btn_special_17,
            R.id.btn_special_18, R.id.btn_special_19
        ).forEach { id ->
            view.findViewById<Button>(id).setOnClickListener {
                commitText((it as Button).text.toString())
            }
        }

        updateShiftState()

        return view
    }

    private fun updateShiftState() {
        btnShift.apply {
            when {
                isShiftLocked -> {
                    setBackgroundColor(Color.parseColor("#FF8888"))
                    setTextColor(Color.BLACK)
                    text = "SHIFT LOCK"
                }
                isShiftOn -> {
                    setBackgroundColor(Color.parseColor("#8888FF"))
                    setTextColor(Color.BLACK)
                    text = "SHIFT ON"
                }
                else -> {
                    setBackgroundColor(Color.parseColor("#444444"))
                    setTextColor(Color.WHITE)
                    text = "SHIFT"
                }
            }
        }
    }

    private fun toggleNumberPad() {
        val newVisibility = if (numberPopup1.isShown) View.GONE else View.VISIBLE
        numberPopup1.visibility = newVisibility
        numberPopup2.visibility = newVisibility
        specialCharPopup.visibility = View.GONE
    }

    private fun toggleSpecialCharPad() {
        val newVisibility = if (specialCharPopup.isShown) View.GONE else View.VISIBLE
        specialCharPopup.visibility = newVisibility
        numberPopup1.visibility = View.GONE
        numberPopup2.visibility = View.GONE
    }

    private fun commitText(text: String) {
        val output = if ((isShiftOn || isShiftLocked) && text.all { it.isLetter() }) {
            text.uppercase()
        } else {
            text
        }
        currentInputConnection?.commitText(output, 1)

        if (isShiftOn && !isShiftLocked && output.any { it.isLetter() }) {
            isShiftOn = false
            updateShiftState()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                currentInputConnection?.deleteSurroundingText(1, 0)
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                requestHideSelf(0)
                return true
            }
        }

        handlePhysicalKey(event)
        return true
    }

    private fun handlePhysicalKey(event: KeyEvent) {
        val ic = currentInputConnection ?: return
        val char = event.unicodeChar.toChar()

        if (char.code == 0) return

        val finalChar = if (numberPopup1.isShown) {
            val numberPadMap = mapOf(
                'w' to '1', 'e' to '2', 'r' to '3',
                's' to '4', 'd' to '5', 'f' to '6',
                'x' to '7', 'c' to '8', 'v' to '9',
                'z' to '0'
            )
            numberPadMap[char.lowercaseChar()]?.toString() ?: char.toString()
        } else {
            if ((isShiftOn || isShiftLocked) && char.isLetter()) {
                char.uppercaseChar().toString()
            } else {
                char.toString()
            }
        }

        ic.commitText(finalChar, 1)

        if (isShiftOn && !isShiftLocked && finalChar.any { it.isLetter() }) {
            isShiftOn = false
            updateShiftState()
        }
    }
}
