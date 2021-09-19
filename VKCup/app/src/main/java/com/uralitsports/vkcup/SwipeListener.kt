package com.uralitsports.vkcup

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

open class SwipeListener(val context:Context):View.OnTouchListener {
    private var gestureDetector:GestureDetector
    private val swipeThreshold = 100

    init {
        gestureDetector= GestureDetector(context,GestureListener())
    }


    private inner class GestureListener:GestureDetector.SimpleOnGestureListener(){
        //always true to trigger on fling
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            onClick()
            return true
        }
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {

            //get the distance from previous to current position
            val distX = e2?.x!! - e1?.x!!
            val distY = e2.y - e1.y

            var swiped = false

            if (distX > swipeThreshold ) onSwipeRight().also { swiped = true }
            else if (distX < -swipeThreshold) onSwipeLeft().also { swiped = true }

            if (distY > swipeThreshold) onSwipeDown().also { swiped = true }
            else if (distY < -swipeThreshold) onSwipeUp().also { swiped = true }


            if (swiped) onSwipeCompleted()


            return true
        }
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }




    open fun onSwipeLeft(){
    }



    open fun onSwipeRight(){
    }



    open fun onSwipeUp(){
    }



    open fun onSwipeDown(){
    }



    open fun onSwipeCompleted(){

    }



    open fun onClick(){

    }
}