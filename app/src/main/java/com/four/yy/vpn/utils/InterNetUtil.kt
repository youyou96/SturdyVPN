package com.four.yy.vpn.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.four.yy.vpn.entity.IpEntity
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.math.roundToInt

open class InterNetUtil {
    suspend fun delayTest(ip: String, timeout: Int = 1): Int {
        var delay = Int.MAX_VALUE
        val count = 1
        val cmd = "/system/bin/ping -c $count -w $timeout $ip"
        return withContext(Dispatchers.IO) {
            val r = ping(cmd)
            if (r != null) {
                try {
                    val index: Int = r.indexOf("min/avg/max/mdev")
                    if (index != -1) {
                        val tempInfo: String = r.substring(index + 19)
                        val temps = tempInfo.split("/".toRegex()).toTypedArray()
                        delay = temps[0].toFloat().roundToInt()//min
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            delay
        }
    }

    /*ping命令*/
    private fun ping(cmd: String): String? {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(cmd) //执行ping指令
            val inputStream = process!!.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (null != reader.readLine().also { line = it }) {
                sb.append(line)
                sb.append("\n")
            }
            reader.close()
            inputStream.close()
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            process?.destroy()
        }
        return null
    }

    fun getIpByServer(context: Context) {
        OkGo.get<String>("https://ip.seeip.org/geoip/")
            .tag(context)
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    SPUtils.get().putString(Constant.iR, response?.body())
                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                }
            })
    }

    fun isNetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        val connected = networkInfo!!.isConnected
        if (connected) {
            return networkInfo.state == NetworkInfo.State.CONNECTED
        }
        return false
    }

    open fun isShowIR(): Boolean {
        val countryList = arrayOf("IR", "CN", "HK", "MO", "MAC")
        val irJson = SPUtils.get().getString(Constant.iR, "")
        val ipEntity = Gson().fromJson(irJson, IpEntity::class.java)
        var ipString = Locale.getDefault().country
        if (ipEntity != null && ipEntity.country.isNotEmpty()) {
            ipString += ipEntity.country
        }
        for (item in countryList) {
            if (ipString.contains(item)) {
                return true
            }
        }
        return false
    }

}