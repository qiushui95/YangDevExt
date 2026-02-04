package zzz.me.yang.dev.ext.utils

import android.app.ActivityManager
import android.app.Application
import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.file.FileSystems
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Date
import java.util.Formatter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

public object LogUtils {
    public const val V: Int = Log.VERBOSE
    public const val D: Int = Log.DEBUG
    public const val I: Int = Log.INFO
    public const val W: Int = Log.WARN
    public const val E: Int = Log.ERROR
    public const val A: Int = Log.ASSERT

    @IntDef(V, D, I, W, E, A)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class TYPE

    private val T = charArrayOf('V', 'D', 'I', 'W', 'E', 'A')

    private const val FILE: Int = 0x10
    private const val JSON: Int = 0x20
    private const val XML: Int = 0x30

    private val FILE_SEP: String = FileSystems.getDefault().separator ?: File.separator
    private val LINE_SEP: String = FileSystems.getDefault().separator ?: "\n"
    private const val TOP_CORNER: String = "┌"
    private const val MIDDLE_CORNER: String = "├"
    private const val LEFT_BORDER: String = "│ "
    private const val BOTTOM_CORNER: String = "└"

    private const val HALF_SIDE_DIVIDER = "────────────────────────────"
    private const val SIDE_DIVIDER: String = HALF_SIDE_DIVIDER + HALF_SIDE_DIVIDER

    private const val HALF_MIDDLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
    private const val MIDDLE_DIVIDER: String = HALF_MIDDLE_DIVIDER + HALF_MIDDLE_DIVIDER
    private const val TOP_BORDER: String = TOP_CORNER + SIDE_DIVIDER + SIDE_DIVIDER
    private const val MIDDLE_BORDER: String = MIDDLE_CORNER + MIDDLE_DIVIDER + MIDDLE_DIVIDER
    private const val BOTTOM_BORDER: String = BOTTOM_CORNER + SIDE_DIVIDER + SIDE_DIVIDER
    private const val MAX_LEN: Int = 1100
    private const val NOTHING: String = "log nothing"
    private const val NULL: String = "null"
    private const val ARGS: String = "args"
    private const val PLACEHOLDER: String = " "

    private val CONFIG: Config = Config()
    private val simpleDateFormat by lazy {
        SimpleDateFormat("yyyy_MM_dd HH:mm:ss.SSS ", Locale.getDefault())
    }

    private val EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor()

    private val I_FORMATTER_MAP: MutableMap<Class<*>, IFormatter<*>> = ConcurrentHashMap()

    @JvmStatic
    public fun getConfig(): Config = CONFIG

    @JvmStatic
    public fun v(vararg contents: Any?) {
        log(V, CONFIG.globalTag, *contents)
    }

    @JvmStatic
    public fun vTag(tag: String, vararg contents: Any?) {
        log(V, tag, *contents)
    }

    @JvmStatic
    public fun d(vararg contents: Any?) {
        log(D, CONFIG.globalTag, *contents)
    }

    @JvmStatic
    public fun dTag(tag: String, vararg contents: Any?) {
        log(D, tag, *contents)
    }

    @JvmStatic
    public fun i(vararg contents: Any?) {
        log(I, CONFIG.globalTag, *contents)
    }

    @JvmStatic
    public fun iTag(tag: String, vararg contents: Any?) {
        log(I, tag, *contents)
    }

    @JvmStatic
    public fun w(vararg contents: Any?) {
        log(W, CONFIG.globalTag, *contents)
    }

    @JvmStatic
    public fun wTag(tag: String, vararg contents: Any?) {
        log(W, tag, *contents)
    }

    @JvmStatic
    public fun e(vararg contents: Any?) {
        log(E, CONFIG.globalTag, *contents)
    }

    @JvmStatic
    public fun eTag(tag: String, vararg contents: Any?) {
        log(E, tag, *contents)
    }

    @JvmStatic
    public fun a(vararg contents: Any?) {
        log(A, CONFIG.globalTag, *contents)
    }

    @JvmStatic
    public fun aTag(tag: String, vararg contents: Any?) {
        log(A, tag, *contents)
    }

    @JvmStatic
    public fun file(content: Any?) {
        log(FILE or D, CONFIG.globalTag, content)
    }

    @JvmStatic
    public fun file(@TYPE type: Int, content: Any?) {
        log(FILE or type, CONFIG.globalTag, content)
    }

    @JvmStatic
    public fun file(tag: String, content: Any?) {
        log(FILE or D, tag, content)
    }

    @JvmStatic
    public fun file(@TYPE type: Int, tag: String, content: Any?) {
        log(FILE or type, tag, content)
    }

    @JvmStatic
    public fun json(content: Any?) {
        log(JSON or D, CONFIG.globalTag, content)
    }

    @JvmStatic
    public fun json(@TYPE type: Int, content: Any?) {
        log(JSON or type, CONFIG.globalTag, content)
    }

    @JvmStatic
    public fun json(tag: String, content: Any?) {
        log(JSON or D, tag, content)
    }

    @JvmStatic
    public fun json(@TYPE type: Int, tag: String, content: Any?) {
        log(JSON or type, tag, content)
    }

    @JvmStatic
    public fun xml(content: String?) {
        log(XML or D, CONFIG.globalTag, content)
    }

    @JvmStatic
    public fun xml(@TYPE type: Int, content: String?) {
        log(XML or type, CONFIG.globalTag, content)
    }

    @JvmStatic
    public fun xml(tag: String, content: String?) {
        log(XML or D, tag, content)
    }

    @JvmStatic
    public fun xml(@TYPE type: Int, tag: String, content: String?) {
        log(XML or type, tag, content)
    }

    @JvmStatic
    public fun log(type: Int, tag: String?, vararg contents: Any?) {
        if (!CONFIG.logSwitch) return
        val typeLow = type and 0x0f
        val typeHigh = type and 0xf0
        if (CONFIG.log2ConsoleSwitch || CONFIG.log2FileSwitch || typeHigh == FILE) {
            if (typeLow < CONFIG.mConsoleFilter && typeLow < CONFIG.mFileFilter) return
            val tagHead = processTagAndHead(tag)
            val body = processBody(typeHigh, *contents)
            if (CONFIG.log2ConsoleSwitch && typeHigh != FILE && typeLow >= CONFIG.mConsoleFilter) {
                print2Console(typeLow, tagHead.tag, tagHead.consoleHead, body)
            }
            if ((CONFIG.log2FileSwitch || typeHigh == FILE) && typeLow >= CONFIG.mFileFilter) {
                EXECUTOR.execute {
                    print2File(typeLow, tagHead.tag, tagHead.fileHead + body)
                }
            }
        }
    }

    @JvmStatic
    public fun getCurrentLogFilePath(): String = getCurrentLogFilePath(Date())

    @JvmStatic
    public fun getLogFiles(): List<File> {
        return File(CONFIG.dir)
            .listFiles { _, name -> isMatchLogFileName(name) }
            ?.toList()
            .orEmpty()
    }

    private fun processTagAndHead(tag: String?): TagHead {
        if (!CONFIG.mTagIsSpace && !CONFIG.logHeadSwitch) {
            return TagHead(CONFIG.globalTag, null, ": ")
        }

        val stackTrace = Throwable().stackTrace
        val stackIndex = 3 + CONFIG.stackOffset
        val isStackIndexValid = stackIndex < stackTrace.size
        val targetElement = if (isStackIndexValid) {
            stackTrace[stackIndex]
        } else {
            stackTrace.getOrNull(3)
        } ?: return TagHead(CONFIG.globalTag, null, ": ")

        var newTag = tag ?: ""
        if (CONFIG.mTagIsSpace && UtilsBridge.isSpace(newTag)) {
            val fileName = getFileName(targetElement)
            val index = fileName.indexOf('.')
            newTag = if (index == -1) fileName else fileName.substring(0, index)
        }

        if (!isStackIndexValid || !CONFIG.logHeadSwitch) {
            return TagHead(newTag, null, ": ")
        }

        val tName = Thread.currentThread().name
        val head = String.format(
            Locale.getDefault(),
            "%s, %s.%s(%s:%d)",
            tName,
            targetElement.className,
            targetElement.methodName,
            getFileName(targetElement),
            targetElement.lineNumber
        )
        val fileHead = " [$head]: "

        if (CONFIG.stackDeep <= 1) {
            return TagHead(newTag, arrayOf(head), fileHead)
        }

        val maxDepth = minOf(CONFIG.stackDeep, stackTrace.size - stackIndex)
        val consoleHead = Array(maxDepth) { "" }
        consoleHead[0] = head
        val space = String.format(Locale.getDefault(), "%${tName.length + 2}s", "")

        for (i in 1 until maxDepth) {
            val element = stackTrace[i + stackIndex]
            consoleHead[i] = String.format(
                Locale.getDefault(),
                "%s%s.%s(%s:%d)",
                space,
                element.className,
                element.methodName,
                getFileName(element),
                element.lineNumber
            )
        }
        return TagHead(newTag, consoleHead, fileHead)
    }

    private fun getFileName(targetElement: StackTraceElement): String {
        targetElement.fileName?.let { return it }
        val className = targetElement.className
            .substringAfterLast('.')
            .substringBefore('$')
        return "$className.java"
    }

    private fun processBody(type: Int, vararg contents: Any?): String {
        if (contents.isEmpty()) return NULL

        val body = if (contents.size == 1) {
            formatObject(type, contents[0])
        } else {
            StringBuilder().apply {
                contents.forEachIndexed { i, content ->
                    append(ARGS)
                        .append("[")
                        .append(i)
                        .append("]")
                        .append(" = ")
                        .append(formatObject(content))
                        .append(LINE_SEP)
                }
            }.toString()
        }
        return body.ifEmpty { NOTHING }
    }

    private fun formatObject(type: Int, obj: Any?): String {
        obj ?: return NULL
        return when (type) {
            JSON, XML -> LogFormatter.object2String(obj, type)
            else -> formatObject(obj)
        }
    }

    private fun formatObject(obj: Any?): String {
        obj ?: return NULL
        if (I_FORMATTER_MAP.isNotEmpty()) {
            val iFormatter = I_FORMATTER_MAP[getClassFromObject(obj)]
            if (iFormatter != null) {
                @Suppress("UNCHECKED_CAST")
                return (iFormatter as IFormatter<Any?>).format(obj)
            }
        }
        return LogFormatter.object2String(obj)
    }

    private fun print2Console(type: Int, tag: String, head: Array<String>?, msg: String) {
        if (CONFIG.singleTagSwitch) {
            printSingleTagMsg(type, tag, processSingleTagMsg(head, msg))
        } else {
            printBorder(type, tag, true)
            printHead(type, tag, head)
            printMsg(type, tag, msg)
            printBorder(type, tag, false)
        }
    }

    private fun printBorder(type: Int, tag: String, isTop: Boolean) {
        if (CONFIG.logBorderSwitch.not()) {
            return
        }

        print2Console(type, tag, if (isTop) TOP_BORDER else BOTTOM_BORDER)
    }

    private fun printHead(type: Int, tag: String, head: Array<String>?) {
        head ?: return

        for (aHead in head) {
            print2Console(type, tag, if (CONFIG.logBorderSwitch) LEFT_BORDER + aHead else aHead)
        }

        if (CONFIG.logBorderSwitch) print2Console(type, tag, MIDDLE_BORDER)
    }

    private fun printMsg(type: Int, tag: String, msg: String) {
        msg.chunked(MAX_LEN).forEach {
            printSubMsg(type, tag, it)
        }
    }

    private fun printSubMsg(type: Int, tag: String, msg: String) {
        if (!CONFIG.logBorderSwitch) {
            print2Console(type, tag, msg)
            return
        }
        val lines = msg.split(LINE_SEP)
        for (line in lines) {
            print2Console(type, tag, LEFT_BORDER + line)
        }
    }

    private fun processSingleTagMsg(head: Array<String>?, msg: String): String {
        return buildString {
            if (CONFIG.logBorderSwitch) {
                append(PLACEHOLDER).append(LINE_SEP)
                append(TOP_BORDER).append(LINE_SEP)
                head?.forEach {
                    append(LEFT_BORDER).append(it).append(LINE_SEP)
                }
                if (head != null) {
                    append(MIDDLE_BORDER).append(LINE_SEP)
                }
                msg.split(LINE_SEP).forEach {
                    append(LEFT_BORDER).append(it).append(LINE_SEP)
                }
                append(BOTTOM_BORDER)
            } else {
                head?.let {
                    append(PLACEHOLDER).append(LINE_SEP)
                    it.forEach { h -> append(h).append(LINE_SEP) }
                }
                append(msg)
            }
        }
    }

    private fun printSingleTagMsg(type: Int, tag: String, msg: String) {
        val len = msg.length
        val countOfSub = if (CONFIG.logBorderSwitch) {
            (len - BOTTOM_BORDER.length) / MAX_LEN
        } else {
            len / MAX_LEN
        }
        if (countOfSub > 0) {
            if (CONFIG.logBorderSwitch) {
                print2Console(type, tag, msg.substring(0, MAX_LEN) + LINE_SEP + BOTTOM_BORDER)
                var index = MAX_LEN
                for (i in 1 until countOfSub) {
                    print2Console(
                        type,
                        tag,
                        PLACEHOLDER + LINE_SEP + TOP_BORDER + LINE_SEP +
                            LEFT_BORDER + msg.substring(index, index + MAX_LEN) +
                            LINE_SEP + BOTTOM_BORDER,
                    )
                    index += MAX_LEN
                }
                if (index != len - BOTTOM_BORDER.length) {
                    print2Console(
                        type,
                        tag,
                        PLACEHOLDER + LINE_SEP + TOP_BORDER + LINE_SEP +
                            LEFT_BORDER + msg.substring(index, len),
                    )
                }
            } else {
                print2Console(type, tag, msg.substring(0, MAX_LEN))
                var index = MAX_LEN
                for (i in 1 until countOfSub) {
                    print2Console(
                        type,
                        tag,
                        PLACEHOLDER + LINE_SEP + msg.substring(index, index + MAX_LEN),
                    )
                    index += MAX_LEN
                }
                if (index != len) {
                    print2Console(type, tag, PLACEHOLDER + LINE_SEP + msg.substring(index, len))
                }
            }
        } else {
            print2Console(type, tag, msg)
        }
    }

    private fun print2Console(type: Int, tag: String, msg: String) {
        Log.println(type, tag, msg)
        CONFIG.mOnConsoleOutputListener?.onConsoleOutput(type, tag, msg)
    }

    private fun print2File(type: Int, tag: String, msg: String) {
        val d = Date()
        val format = simpleDateFormat.format(d)
        val date = format.substring(0, 10)
        val currentLogFilePath = getCurrentLogFilePath(d)
        if (!createOrExistsFile(currentLogFilePath, date)) {
            Log.e("LogUtils", "create $currentLogFilePath failed!")
            return
        }
        val time = format.substring(11)
        val content = time + T[type - V] + "/" + tag + msg + LINE_SEP
        input2File(currentLogFilePath, content)
    }

    private fun getCurrentLogFilePath(d: Date): String {
        val format = simpleDateFormat.format(d)
        val date = format.substring(0, 10)
        return CONFIG.dir +
            CONFIG.filePrefix +
            "_" +
            date +
            "_" +
            CONFIG.processName +
            CONFIG.fileExtension
    }

    private fun createOrExistsFile(filePath: String, date: String): Boolean {
        val file = File(filePath)
        if (file.exists()) return file.isFile
        if (!UtilsBridge.createOrExistsDir(file.parentFile)) return false
        return try {
            deleteDueLogs(filePath, date)
            val isCreate = file.createNewFile()
            if (isCreate) {
                printDeviceInfo(filePath, date)
            }
            isCreate
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun deleteDueLogs(filePath: String, date: String) {
        if (CONFIG.saveDays <= 0) return
        val file = File(filePath)
        val parentFile = file.parentFile ?: return
        val files = parentFile.listFiles(FilenameFilter { _, name -> isMatchLogFileName(name) })
        if (files.isNullOrEmpty()) return
        val sdf = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault())
        try {
            val dueMillis = sdf.parse(date)!!.time - CONFIG.saveDays * 86400000L
            for (aFile in files) {
                val name = aFile.name
                val logDay = findDate(name)
                if (sdf.parse(logDay)!!.time <= dueMillis) {
                    EXECUTOR.execute {
                        val delete = aFile.delete()
                        if (!delete) {
                            Log.e("LogUtils", "delete $aFile failed!")
                        }
                    }
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun isMatchLogFileName(name: String): Boolean {
        return name.matches(("^" + CONFIG.filePrefix + "_[0-9]{4}_[0-9]{2}_[0-9]{2}_.*$").toRegex())
    }

    private fun findDate(str: String): String {
        val pattern = Pattern.compile("[0-9]{4}_[0-9]{2}_[0-9]{2}")
        val matcher = pattern.matcher(str)
        return if (matcher.find()) matcher.group() ?: "" else ""
    }

    private fun printDeviceInfo(filePath: String, date: String) {
        CONFIG.addFileHeadFirst("Date of Log", date)
        input2File(filePath, CONFIG.getFileHeadString())
    }

    private fun input2File(filePath: String, input: String) {
        if (CONFIG.mFileWriter == null) {
            UtilsBridge.writeFileFromString(filePath, input, true)
        } else {
            CONFIG.mFileWriter?.write(filePath, input)
        }
        CONFIG.mOnFileOutputListener?.onFileOutput(filePath, input)
    }

    public class Config internal constructor() {
        internal var mDefaultDir: String
        internal var mDir: String? = null
        internal var mFilePrefix: String = "util"
        internal var mFileExtension: String = ".txt"
        internal var mLogSwitch: Boolean = true
        internal var mLog2ConsoleSwitch: Boolean = true
        internal var mGlobalTag: String = ""
        internal var mTagIsSpace: Boolean = true
        internal var mLogHeadSwitch: Boolean = true
        internal var mLog2FileSwitch: Boolean = false
        internal var mLogBorderSwitch: Boolean = true
        internal var mSingleTagSwitch: Boolean = true
        internal var mConsoleFilter: Int = V
        internal var mFileFilter: Int = V
        internal var mStackDeep: Int = 1
        internal var mStackOffset: Int = 0
        internal var mSaveDays: Int = -1
        internal var mProcessName: String? = UtilsBridge.getCurrentProcessName()
        internal var mFileWriter: IFileWriter? = null
        internal var mOnConsoleOutputListener: OnConsoleOutputListener? = null
        internal var mOnFileOutputListener: OnFileOutputListener? = null
        private val fileHead: UtilsBridge.FileHead = UtilsBridge.FileHead("Log")

        init {
            mDefaultDir =
                if (
                    UtilsBridge.isSDCardEnableByEnvironment() &&
                    Utils.getApp().getExternalFilesDir(null) != null
                ) {
                    Utils.getApp().getExternalFilesDir(null)!!.toString() +
                        FILE_SEP +
                        "log" +
                        FILE_SEP
                } else {
                    Utils.getApp().filesDir.toString() + FILE_SEP + "log" + FILE_SEP
                }
        }

        public fun setLogSwitch(logSwitch: Boolean): Config {
            mLogSwitch = logSwitch
            return this
        }

        public fun setConsoleSwitch(consoleSwitch: Boolean): Config {
            mLog2ConsoleSwitch = consoleSwitch
            return this
        }

        public fun setGlobalTag(tag: String?): Config {
            if (UtilsBridge.isSpace(tag)) {
                mGlobalTag = ""
                mTagIsSpace = true
            } else {
                mGlobalTag = tag ?: ""
                mTagIsSpace = false
            }
            return this
        }

        public fun setLogHeadSwitch(logHeadSwitch: Boolean): Config {
            mLogHeadSwitch = logHeadSwitch
            return this
        }

        public fun setLog2FileSwitch(log2FileSwitch: Boolean): Config {
            mLog2FileSwitch = log2FileSwitch
            return this
        }

        public fun setDir(dir: String?): Config {
            mDir = if (UtilsBridge.isSpace(dir)) {
                null
            } else {
                if (dir!!.endsWith(FILE_SEP)) dir else dir + FILE_SEP
            }
            return this
        }

        public fun setDir(dir: File?): Config {
            mDir = if (dir == null) null else dir.absolutePath + FILE_SEP
            return this
        }

        public fun setFilePrefix(filePrefix: String?): Config {
            mFilePrefix = if (UtilsBridge.isSpace(filePrefix)) "util" else filePrefix!!
            return this
        }

        public fun setFileExtension(fileExtension: String?): Config {
            mFileExtension = if (UtilsBridge.isSpace(fileExtension)) {
                ".txt"
            } else {
                if (fileExtension!!.startsWith(".")) fileExtension else ".$fileExtension"
            }
            return this
        }

        public fun setBorderSwitch(borderSwitch: Boolean): Config {
            mLogBorderSwitch = borderSwitch
            return this
        }

        public fun setSingleTagSwitch(singleTagSwitch: Boolean): Config {
            mSingleTagSwitch = singleTagSwitch
            return this
        }

        public fun setConsoleFilter(@TYPE consoleFilter: Int): Config {
            mConsoleFilter = consoleFilter
            return this
        }

        public fun setFileFilter(@TYPE fileFilter: Int): Config {
            mFileFilter = fileFilter
            return this
        }

        public fun setStackDeep(@IntRange(from = 1) stackDeep: Int): Config {
            mStackDeep = stackDeep
            return this
        }

        public fun setStackOffset(@IntRange(from = 0) stackOffset: Int): Config {
            mStackOffset = stackOffset
            return this
        }

        public fun setSaveDays(@IntRange(from = 1) saveDays: Int): Config {
            mSaveDays = saveDays
            return this
        }

        public fun <T> addFormatter(iFormatter: IFormatter<T>?): Config {
            if (iFormatter != null) {
                val cls = getTypeClassFromParadigm(iFormatter)
                if (cls != null) {
                    I_FORMATTER_MAP[cls] = iFormatter
                }
            }
            return this
        }

        public fun setFileWriter(fileWriter: IFileWriter?): Config {
            mFileWriter = fileWriter
            return this
        }

        public fun setOnConsoleOutputListener(listener: OnConsoleOutputListener?): Config {
            mOnConsoleOutputListener = listener
            return this
        }

        public fun setOnFileOutputListener(listener: OnFileOutputListener?): Config {
            mOnFileOutputListener = listener
            return this
        }

        public fun addFileExtraHead(fileExtraHead: Map<String, String>?): Config {
            fileHead.append(fileExtraHead)
            return this
        }

        public fun addFileExtraHead(key: String?, value: String?): Config {
            fileHead.append(key, value)
            return this
        }

        internal fun addFileHeadFirst(key: String, value: String) {
            fileHead.addFirst(key, value)
        }

        internal fun getFileHeadString(): String = fileHead.toString()

        public val processName: String
            get() = (mProcessName ?: "").replace(":", "_")

        public val defaultDir: String
            get() = mDefaultDir

        public val dir: String
            get() = mDir ?: mDefaultDir

        public val filePrefix: String
            get() = mFilePrefix

        public val fileExtension: String
            get() = mFileExtension

        public val logSwitch: Boolean
            get() = mLogSwitch

        public val log2ConsoleSwitch: Boolean
            get() = mLog2ConsoleSwitch

        public val globalTag: String
            get() = if (UtilsBridge.isSpace(mGlobalTag)) "" else mGlobalTag

        public val logHeadSwitch: Boolean
            get() = mLogHeadSwitch

        public val log2FileSwitch: Boolean
            get() = mLog2FileSwitch

        public val logBorderSwitch: Boolean
            get() = mLogBorderSwitch

        public val singleTagSwitch: Boolean
            get() = mSingleTagSwitch

        public val consoleFilter: Char
            get() = T[mConsoleFilter - V]

        public val fileFilter: Char
            get() = T[mFileFilter - V]

        public val stackDeep: Int
            get() = mStackDeep

        public val stackOffset: Int
            get() = mStackOffset

        public val saveDays: Int
            get() = mSaveDays

        public fun haveSetOnConsoleOutputListener(): Boolean = mOnConsoleOutputListener != null

        public fun haveSetOnFileOutputListener(): Boolean = mOnFileOutputListener != null

        override fun toString(): String {
            return "process: " + processName +
                LINE_SEP + "logSwitch: " + logSwitch +
                LINE_SEP + "consoleSwitch: " + log2ConsoleSwitch +
                LINE_SEP + "tag: " + (if (globalTag == "") "null" else globalTag) +
                LINE_SEP + "headSwitch: " + logHeadSwitch +
                LINE_SEP + "fileSwitch: " + log2FileSwitch +
                LINE_SEP + "dir: " + dir +
                LINE_SEP + "filePrefix: " + filePrefix +
                LINE_SEP + "borderSwitch: " + logBorderSwitch +
                LINE_SEP + "singleTagSwitch: " + singleTagSwitch +
                LINE_SEP + "consoleFilter: " + consoleFilter +
                LINE_SEP + "fileFilter: " + fileFilter +
                LINE_SEP + "stackDeep: " + stackDeep +
                LINE_SEP + "stackOffset: " + stackOffset +
                LINE_SEP + "saveDays: " + saveDays +
                LINE_SEP + "formatter: " + I_FORMATTER_MAP +
                LINE_SEP + "fileWriter: " + mFileWriter +
                LINE_SEP + "onConsoleOutputListener: " + mOnConsoleOutputListener +
                LINE_SEP + "onFileOutputListener: " + mOnFileOutputListener +
                LINE_SEP + "fileExtraHeader: " + fileHead.getAppended()
        }
    }

    public abstract class IFormatter<T> {
        public abstract fun format(t: T): String
    }

    public fun interface IFileWriter {
        public fun write(file: String, content: String)
    }

    public fun interface OnConsoleOutputListener {
        public fun onConsoleOutput(@TYPE type: Int, tag: String, content: String)
    }

    public fun interface OnFileOutputListener {
        public fun onFileOutput(filePath: String, content: String)
    }

    private class TagHead(
        val tag: String,
        val consoleHead: Array<String>?,
        val fileHead: String,
    )

    private object LogFormatter {
        fun object2String(obj: Any): String = object2String(obj, -1)

        fun object2String(obj: Any, type: Int): String {
            if (obj.javaClass.isArray) return array2String(obj)
            if (obj is Throwable) return UtilsBridge.getFullStackTrace(obj)
            if (obj is Bundle) return bundle2String(obj)
            if (obj is Intent) return intent2String(obj)
            if (type == JSON) {
                return object2Json(obj)
            } else if (type == XML) {
                return formatXml(obj.toString())
            }
            return obj.toString()
        }

        private fun bundle2String(bundle: Bundle): String {
            val iterator = bundle.keySet().iterator()
            if (!iterator.hasNext()) {
                return "Bundle {}"
            }
            val sb = StringBuilder(128)
            sb.append("Bundle { ")
            while (true) {
                val key = iterator.next()
                val value = bundle.get(key)
                sb.append(key).append('=')
                if (value is Bundle) {
                    sb.append(if (value === bundle) "(this Bundle)" else bundle2String(value))
                } else {
                    sb.append(formatObject(value))
                }
                if (!iterator.hasNext()) return sb.append(" }").toString()
                sb.append(',').append(' ')
            }
        }

        private fun intent2String(intent: Intent): String {
            val sb = StringBuilder(128)
            sb.append("Intent { ")
            var first = true
            val mAction = intent.action
            if (mAction != null) {
                sb.append("act=").append(mAction)
                first = false
            }
            val mCategories = intent.categories
            if (mCategories != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("cat=[")
                var firstCategory = true
                for (c in mCategories) {
                    if (!firstCategory) {
                        sb.append(',')
                    }
                    sb.append(c)
                    firstCategory = false
                }
                sb.append("]")
            }
            val mData = intent.data
            if (mData != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("dat=").append(mData)
            }
            val mType = intent.type
            if (mType != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("typ=").append(mType)
            }
            val mFlags = intent.flags
            if (mFlags != 0) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("flg=0x").append(Integer.toHexString(mFlags))
            }
            val mPackage = intent.`package`
            if (mPackage != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("pkg=").append(mPackage)
            }
            val mComponent: ComponentName? = intent.component
            if (mComponent != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("cmp=").append(mComponent.flattenToShortString())
            }
            val mSourceBounds: Rect? = intent.sourceBounds
            if (mSourceBounds != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("bnds=").append(mSourceBounds.toShortString())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                val mClipData = intent.clipData
                if (mClipData != null) {
                    if (!first) {
                        sb.append(' ')
                    }
                    first = false
                    clipData2String(mClipData, sb)
                }
            }
            val mExtras = intent.extras
            if (mExtras != null) {
                if (!first) {
                    sb.append(' ')
                }
                first = false
                sb.append("extras={")
                sb.append(bundle2String(mExtras))
                sb.append('}')
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                val mSelector = intent.selector
                if (mSelector != null) {
                    if (!first) {
                        sb.append(' ')
                    }
                    first = false
                    sb.append("sel={")
                    sb.append(
                        if (mSelector === intent) "(this Intent)" else intent2String(mSelector),
                    )
                    sb.append("}")
                }
            }
            sb.append(" }")
            return sb.toString()
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        private fun clipData2String(clipData: ClipData, sb: StringBuilder) {
            val item = clipData.getItemAt(0)
            if (item == null) {
                sb.append("ClipData.Item {}")
                return
            }
            sb.append("ClipData.Item { ")
            val mHtmlText = item.htmlText
            if (mHtmlText != null) {
                sb.append("H:")
                sb.append(mHtmlText)
                sb.append("}")
                return
            }
            val mText = item.text
            if (mText != null) {
                sb.append("T:")
                sb.append(mText)
                sb.append("}")
                return
            }
            val uri = item.uri
            if (uri != null) {
                sb.append("U:").append(uri)
                sb.append("}")
                return
            }
            val intent = item.intent
            if (intent != null) {
                sb.append("I:")
                sb.append(intent2String(intent))
                sb.append("}")
                return
            }
            sb.append("NULL")
            sb.append("}")
        }

        private fun object2Json(obj: Any): String {
            if (obj is CharSequence) {
                return UtilsBridge.formatJson(obj.toString())
            }
            return try {
                val gson = UtilsBridge.getGson4LogUtils()
                val toJson = gson.javaClass.getMethod("toJson", Any::class.java)
                toJson.invoke(gson, obj) as String
            } catch (t: Throwable) {
                obj.toString()
            }
        }

        private fun formatJson(json: String): String {
            try {
                for (i in json.indices) {
                    val c = json[i]
                    if (c == '{') {
                        return JSONObject(json).toString(2)
                    } else if (c == '[') {
                        return JSONArray(json).toString(2)
                    } else if (!c.isWhitespace()) {
                        return json
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return json
        }

        private fun formatXml(xml: String): String {
            var newXml = xml
            try {
                val xmlInput: Source = StreamSource(StringReader(newXml))
                val xmlOutput = StreamResult(StringWriter())
                val transformer = TransformerFactory.newInstance().newTransformer()
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
                transformer.transform(xmlInput, xmlOutput)
                newXml = xmlOutput.writer.toString().replaceFirst(">", ">$LINE_SEP")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return newXml
        }

        private fun array2String(obj: Any): String {
            return when (obj) {
                is Array<*> -> Arrays.deepToString(obj)
                is BooleanArray -> Arrays.toString(obj)
                is ByteArray -> Arrays.toString(obj)
                is CharArray -> Arrays.toString(obj)
                is DoubleArray -> Arrays.toString(obj)
                is FloatArray -> Arrays.toString(obj)
                is IntArray -> Arrays.toString(obj)
                is LongArray -> Arrays.toString(obj)
                is ShortArray -> Arrays.toString(obj)
                else ->
                    throw IllegalArgumentException(
                        "Array has incompatible type: " + obj.javaClass,
                    )
            }
        }
    }

    private fun <T> getTypeClassFromParadigm(formatter: IFormatter<T>): Class<*>? {
        val genericInterfaces = formatter.javaClass.genericInterfaces
        var type: Type = if (genericInterfaces.size == 1) {
            genericInterfaces[0]
        } else {
            formatter.javaClass.genericSuperclass
        }
        type = (type as ParameterizedType).actualTypeArguments[0]
        while (type is ParameterizedType) {
            type = type.rawType
        }
        var className = type.toString()
        if (className.startsWith("class ")) {
            className = className.substring(6)
        } else if (className.startsWith("interface ")) {
            className = className.substring(10)
        }
        return try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private fun getClassFromObject(obj: Any): Class<*> {
        val objClass = obj.javaClass
        if (objClass.isAnonymousClass || objClass.isSynthetic) {
            val genericInterfaces = objClass.genericInterfaces
            var className: String = if (genericInterfaces.size == 1) {
                var type: Type = genericInterfaces[0]
                while (type is ParameterizedType) {
                    type = type.rawType
                }
                type.toString()
            } else {
                var type: Type = objClass.genericSuperclass
                while (type is ParameterizedType) {
                    type = type.rawType
                }
                type.toString()
            }
            if (className.startsWith("class ")) {
                className = className.substring(6)
            } else if (className.startsWith("interface ")) {
                className = className.substring(10)
            }
            return try {
                Class.forName(className)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                objClass
            }
        }
        return objClass
    }

    private object UtilsBridge {
        private var gson4LogUtils: Any? = null

        fun isSpace(s: CharSequence?): Boolean {
            if (s == null) return true
            for (i in 0 until s.length) {
                if (!s[i].isWhitespace()) return false
            }
            return true
        }

        fun createOrExistsDir(file: File?): Boolean {
            if (file == null) return false
            return if (file.exists()) file.isDirectory else file.mkdirs()
        }

        fun writeFileFromString(filePath: String, content: String, append: Boolean): Boolean {
            return try {
                val file = File(filePath)
                val parent = file.parentFile
                if (parent != null && !createOrExistsDir(parent)) return false
                java.io.FileOutputStream(file, append)
                    .bufferedWriter(Charsets.UTF_8)
                    .use { it.write(content) }
                true
            } catch (t: Throwable) {
                t.printStackTrace()
                false
            }
        }

        fun getCurrentProcessName(): String? {
            val app = Utils.getApp()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return Application.getProcessName()
            }
            return try {
                val am = app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val pid = android.os.Process.myPid()
                val processes = am.runningAppProcesses ?: return app.applicationInfo.processName
                for (process in processes) {
                    if (process.pid == pid) {
                        return process.processName
                    }
                }
                app.applicationInfo.processName
            } catch (t: Throwable) {
                t.printStackTrace()
                app.applicationInfo.processName
            }
        }

        fun isSDCardEnableByEnvironment(): Boolean {
            return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
        }

        fun getFullStackTrace(t: Throwable): String {
            return try {
                val sw = StringWriter()
                PrintWriter(sw).use { pw -> t.printStackTrace(pw) }
                sw.toString()
            } catch (e: Throwable) {
                Log.getStackTraceString(t)
            }
        }

        fun formatJson(json: String): String {
            return try {
                for (i in json.indices) {
                    val c = json[i]
                    if (c == '{') {
                        JSONObject(json).toString(2)
                    } else if (c == '[') {
                        JSONArray(json).toString(2)
                    } else if (!c.isWhitespace()) {
                        json
                    } else {
                        continue
                    }.let { return it }
                }
                json
            } catch (t: Throwable) {
                t.printStackTrace()
                json
            }
        }

        fun getGson4LogUtils(): Any {
            val cached = gson4LogUtils
            if (cached != null) return cached
            val created = try {
                Class.forName("com.google.gson.Gson").getConstructor().newInstance()
            } catch (t: Throwable) {
                throw t
            }
            gson4LogUtils = created
            return created
        }

        class FileHead(private val name: String) {
            private val first: LinkedHashMap<String, String> = LinkedHashMap()
            private val last: LinkedHashMap<String, String> = LinkedHashMap()

            fun addFirst(key: String?, value: String?) {
                append2Host(first, key, value)
            }

            fun append(extra: Map<String, String>?) {
                if (extra.isNullOrEmpty()) return
                for ((k, v) in extra) {
                    append2Host(last, k, v)
                }
            }

            fun append(key: String?, value: String?) {
                append2Host(last, key, value)
            }

            private fun append2Host(
                host: MutableMap<String, String>,
                key: String?,
                value: String?,
            ) {
                if (key.isNullOrEmpty() || value.isNullOrEmpty()) return
                var newKey = key
                val delta = 19 - newKey.length
                if (delta > 0) {
                    newKey += "                   ".substring(0, delta)
                }
                host[newKey] = value
            }

            fun getAppended(): String {
                val sb = StringBuilder()
                for ((k, v) in last) {
                    sb.append(k).append(": ").append(v).append("\n")
                }
                return sb.toString()
            }

            override fun toString(): String {
                val sb = StringBuilder()
                val border = "************* $name Head ****************\n"
                sb.append(border)
                for ((k, v) in first) {
                    sb.append(k).append(": ").append(v).append("\n")
                }

                val app = Utils.getApp()
                val pm = app.packageManager
                val pkg = app.packageName
                var versionName = ""
                var versionCode = 0L
                try {
                    @Suppress("DEPRECATION")
                    val pi = pm.getPackageInfo(pkg, 0)
                    versionName = pi.versionName ?: ""
                    versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pi.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        pi.versionCode.toLong()
                    }
                } catch (_: Throwable) {
                }

                sb.append("Rom Info           : ").append(Build.DISPLAY).append("\n")
                sb.append("Device Manufacturer: ").append(Build.MANUFACTURER).append("\n")
                sb.append("Device Model       : ").append(Build.MODEL).append("\n")
                sb.append("Android Version    : ").append(Build.VERSION.RELEASE).append("\n")
                sb.append("Android SDK        : ").append(Build.VERSION.SDK_INT).append("\n")
                sb.append("App VersionName    : ").append(versionName).append("\n")
                sb.append("App VersionCode    : ").append(versionCode).append("\n")

                sb.append(getAppended())
                return sb.append(border).append("\n").toString()
            }
        }
    }
}
