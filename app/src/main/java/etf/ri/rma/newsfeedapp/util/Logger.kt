package etf.ri.rma.newsfeedapp.util

import android.util.Log
import etf.ri.rma.newsfeedapp.BuildConfig

object Logger {
    private const val TAG = "NewsFeedApp"

    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }

    fun i(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    fun w(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }
}
