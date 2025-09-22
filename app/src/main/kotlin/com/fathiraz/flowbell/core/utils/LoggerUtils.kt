package com.fathiraz.flowbell.core.utils

import android.util.Log
import timber.log.Timber

/**
 * Unified logging utility that writes to both Android Log and Timber
 *
 * This utility ensures all logs are captured by:
 * - Android's built-in logging system (accessible via logcat)
 * - Timber (accessible via Hyperion and other debugging tools)
 *
 * Usage:
 * - LoggerUtils.d(tag, message) for debug logs
 * - LoggerUtils.i(tag, message) for info logs
 * - LoggerUtils.w(tag, message, throwable) for warning logs
 * - LoggerUtils.e(tag, message, throwable) for error logs
 */
object LoggerUtils {

    private const val DEFAULT_TAG = "FlowBell"

    /**
     * Debug level logging
     */
    fun d(tag: String = DEFAULT_TAG, message: String) {
        Log.d(tag, message)
        Timber.tag(tag).d(message)
    }

    fun d(tag: String = DEFAULT_TAG, message: String, vararg args: Any?) {
        val formattedMessage = String.format(message, *args)
        Log.d(tag, formattedMessage)
        Timber.tag(tag).d(formattedMessage)
    }

    /**
     * Info level logging
     */
    fun i(tag: String = DEFAULT_TAG, message: String) {
        Log.i(tag, message)
        Timber.tag(tag).i(message)
    }

    fun i(tag: String = DEFAULT_TAG, message: String, vararg args: Any?) {
        val formattedMessage = String.format(message, *args)
        Log.i(tag, formattedMessage)
        Timber.tag(tag).i(formattedMessage)
    }

    /**
     * Warning level logging
     */
    fun w(tag: String = DEFAULT_TAG, message: String) {
        Log.w(tag, message)
        Timber.tag(tag).w(message)
    }

    fun w(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        Timber.tag(tag).w(throwable, message)
    }

    fun w(tag: String = DEFAULT_TAG, throwable: Throwable, message: String, vararg args: Any?) {
        val formattedMessage = String.format(message, *args)
        Log.w(tag, formattedMessage, throwable)
        Timber.tag(tag).w(throwable, formattedMessage)
    }

    /**
     * Error level logging
     */
    fun e(tag: String = DEFAULT_TAG, message: String) {
        Log.e(tag, message)
        Timber.tag(tag).e(message)
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        Timber.tag(tag).e(throwable, message)
    }

    fun e(tag: String = DEFAULT_TAG, throwable: Throwable, message: String, vararg args: Any?) {
        val formattedMessage = String.format(message, *args)
        Log.e(tag, formattedMessage, throwable)
        Timber.tag(tag).e(throwable, formattedMessage)
    }

    /**
     * Verbose level logging
     */
    fun v(tag: String = DEFAULT_TAG, message: String) {
        Log.v(tag, message)
        Timber.tag(tag).v(message)
    }

    fun v(tag: String = DEFAULT_TAG, message: String, vararg args: Any?) {
        val formattedMessage = String.format(message, *args)
        Log.v(tag, formattedMessage)
        Timber.tag(tag).v(formattedMessage)
    }

    /**
     * What a Terrible Failure logging (critical errors)
     */
    fun wtf(tag: String = DEFAULT_TAG, message: String) {
        Log.wtf(tag, message)
        Timber.tag(tag).wtf(message)
    }

    fun wtf(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.wtf(tag, message, throwable)
        Timber.tag(tag).wtf(throwable, message)
    }

    /**
     * Convenience methods for component-specific logging
     */
    object App {
        private const val TAG = "FlowBell-App"
        fun d(message: String) = d(TAG, message)
        fun i(message: String) = i(TAG, message)
        fun w(message: String, throwable: Throwable? = null) = w(TAG, message, throwable)
        fun e(message: String, throwable: Throwable? = null) = e(TAG, message, throwable)
    }

    object Database {
        private const val TAG = "FlowBell-Database"
        fun d(message: String) = d(TAG, message)
        fun i(message: String) = i(TAG, message)
        fun w(message: String, throwable: Throwable? = null) = w(TAG, message, throwable)
        fun e(message: String, throwable: Throwable? = null) = e(TAG, message, throwable)
    }

    object Network {
        private const val TAG = "FlowBell-Network"
        fun d(message: String) = d(TAG, message)
        fun i(message: String) = i(TAG, message)
        fun w(message: String, throwable: Throwable? = null) = w(TAG, message, throwable)
        fun e(message: String, throwable: Throwable? = null) = e(TAG, message, throwable)
    }

    object Notification {
        private const val TAG = "FlowBell-Notification"
        fun d(message: String) = d(TAG, message)
        fun i(message: String) = i(TAG, message)
        fun w(message: String, throwable: Throwable? = null) = w(TAG, message, throwable)
        fun e(message: String, throwable: Throwable? = null) = e(TAG, message, throwable)
    }

    object Webhook {
        private const val TAG = "FlowBell-Webhook"
        fun d(message: String) = d(TAG, message)
        fun i(message: String) = i(TAG, message)
        fun w(message: String, throwable: Throwable? = null) = w(TAG, message, throwable)
        fun e(message: String, throwable: Throwable? = null) = e(TAG, message, throwable)
    }

    object DataStore {
        private const val TAG = "FlowBell-DataStore"
        fun d(message: String) = d(TAG, message)
        fun i(message: String) = i(TAG, message)
        fun w(message: String, throwable: Throwable? = null) = w(TAG, message, throwable)
        fun e(message: String, throwable: Throwable? = null) = e(TAG, message, throwable)
    }
}