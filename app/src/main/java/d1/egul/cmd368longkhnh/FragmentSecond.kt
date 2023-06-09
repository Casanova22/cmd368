package d1.egul.cmd368longkhnh

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import d1.egul.cmd368longkhnh.databinding.FragmentSecondBinding
import d1.egul.cmd368longkhnh.service.NetworkHandlerCmd
import d1.egul.cmd368longkhnh.view.ViewModelCmd

class FragmentSecond : Fragment() {

    private var fragmentSecondBinding : FragmentSecondBinding? = null
    private val _webBinding get() = fragmentSecondBinding!!

    private lateinit var viewModel : ViewModelCmd

    private var connCheck = NetworkHandlerCmd()
    private var checkNet = false

    var title: String? = null
    var imgUrl: String? = null

    private val downloadListener = DownloadListener { p0, _, _, _, _ ->
        val uri = Uri.parse(p0)
        val a = Intent(Intent.ACTION_VIEW, uri)
        context?.startActivity(a)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        /*val gradientDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient) as GradientDrawable

        val valueAnimator = ValueAnimator.ofArgb(
            0xA2A2A2,
            0x000000,
            0xA2A2A2,
            0xA2A2A2
        ).apply {
            duration = 500
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                gradientDrawable.colors = intArrayOf(color, color, color)
                _webBinding.navTitle.background = gradientDrawable
            }
        }
*/
        fragmentSecondBinding = FragmentSecondBinding.inflate(inflater,container,false)

        viewModel = ViewModelProvider(this)[ViewModelCmd::class.java]
/*        valueAnimator.start()*/
        checkNetConnection()
        title = arguments?.getString("title")
        return _webBinding.root
    }

    private fun checkNetConnection() {
        checkNet = connCheck.connectionError(requireActivity())
        if (checkNet) {
            viewModel.initJson()
            getJumpCode()
        } else {
            Toast.makeText(context, "Connection error, Please check internet connection.",
                Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getJumpCode() {
        viewModel.dataListModel.observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    for (i in it.indices) {
                        val name = it[i].PathName
                        val url = it[i].Url
                        val status = it[i].Status
                        val webView: WebView = _webBinding.mainWebView
                        if (name == context?.packageName) {
                            Log.e(ContentValues.TAG, name.toString())
                            when (status) {
                                true -> {
                                    webView.loadUrl(url.toString())
                                }
                                else -> {
                                    if (title == "webView") {
                                        webView.loadUrl("file:///android_asset/nurse/health.html")
                                    } else {
                                        webView.loadUrl("file:///android_asset/nurse/health.html")
                                    }
                                }
                            }
                            webViewInit()
                            activity?.overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewInit() {
        with(_webBinding.mainWebView) {
            with(settings) {
                javaScriptEnabled = true
                defaultTextEncodingName = "UTF-8"
                cacheMode = WebSettings.LOAD_NO_CACHE
                useWideViewPort = true
                pluginState = WebSettings.PluginState.ON
                domStorageEnabled = true
                builtInZoomControls = false
                layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
                loadWithOverviewMode = true
                blockNetworkImage = true
                loadsImagesAutomatically = true
                setSupportZoom(false)
                setSupportMultipleWindows(true)
            }
            requestFocusFromTouch()
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            setDownloadListener(downloadListener)
        }

        val webSetting: WebSettings = _webBinding.mainWebView.settings
        with(webSetting) {
            context?.getDir(
                "cache", AppCompatActivity.MODE_PRIVATE
            )?.path
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        _webBinding.mainWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                _webBinding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    _webBinding.mainWebView.settings.blockNetworkImage = false
                }
            }

            override fun onCreateWindow(
                view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message,
            ): Boolean {
                val newWebView = context?.let { WebView(it) }
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()
                newWebView?.webViewClient = object : WebViewClient() {
                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        _webBinding.mainWebView.loadUrl(url)
                        if (url.startsWith("http") || url.startsWith("https")) {
                            return super.shouldOverrideUrlLoading(view, url)
                        } else if (url.startsWith(WebView.SCHEME_TEL) || url.startsWith(WebView.SCHEME_MAILTO)) {
                            val dialIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(dialIntent)
                        } else {
                            try {
                                context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

                            } catch (ex: ActivityNotFoundException) {
                                val makeShortText = "The Application has not been installed"
                                Toast.makeText(context, makeShortText, Toast.LENGTH_SHORT).show()
                            }
                        }
                        return true
                    }
                }
                return true
            }
        }

        val settings: WebSettings = _webBinding.mainWebView.settings
        settings.javaScriptEnabled = true
        _webBinding.mainWebView.setOnLongClickListener { v: View ->
            val result = (v as WebView).hitTestResult
            val type = result.type
            if (type == WebView.HitTestResult.UNKNOWN_TYPE) return@setOnLongClickListener false
            when (type) {
                WebView.HitTestResult.PHONE_TYPE -> {}
                WebView.HitTestResult.EMAIL_TYPE -> {}
                WebView.HitTestResult.GEO_TYPE -> {}
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {}
                WebView.HitTestResult.IMAGE_TYPE -> {
                    imgUrl = result.extra
                }
                else -> {}
            }
            true
        }

        _webBinding.mainWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                _webBinding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                _webBinding.progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError,
            ) {
                super.onReceivedError(view, request, error)
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler, error: SslError,
            ) {
                val builder = android.app.AlertDialog.Builder(context)
                var message = "SSL Certificate error."
                when (error.primaryError) {
                    SslError.SSL_UNTRUSTED -> message = "The certificate authority is not trusted."
                    SslError.SSL_EXPIRED -> message = "The certificate has expired."
                    SslError.SSL_IDMISMATCH -> message = "The certificate Hostname mismatch."
                    SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid."
                }
                message += " Do you want to continue anyway?"
                builder.setTitle("SSL Certificate Error")
                builder.setMessage(message)
                builder.setPositiveButton(
                    "Continue"
                ) { _: DialogInterface?, _: Int -> handler.proceed() }
                builder.setNegativeButton(
                    "Cancel"
                ) { _: DialogInterface?, _: Int -> handler.cancel() }
                val dialog = builder.create()
                dialog.show()
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("http") || url.startsWith("https")) {
                    return super.shouldOverrideUrlLoading(view, url)
                } else if (url.startsWith("intent:")) {
                    val urlSplit = url.split("/").toTypedArray()
                    var send = ""
                    if (urlSplit[2] == "user") {
                        send = "https://m.me/" + urlSplit[3]
                    } else if (urlSplit[2] == "ti") {
                        val data = urlSplit[4]
                        val newSplit = data.split("#").toTypedArray()
                        send = "https://line.me/R/" + newSplit[0]
                    }
                    val newInt = Intent(Intent.ACTION_VIEW, Uri.parse(send))
                    context?.startActivity(newInt)
                } else {
                    try {
                        context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (ex: ActivityNotFoundException) {
                        val makeShortText = "The Application has not been installed"
                        Toast.makeText(context, makeShortText, Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }
        }

        _webBinding.mainWebView.setOnKeyListener { _: View?, i: Int, keyEvent: KeyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                if (i == KeyEvent.KEYCODE_BACK && _webBinding.mainWebView.canGoBack()) {
                    _webBinding.mainWebView.goBack()
                    return@setOnKeyListener true
                }
            }
            false
        }
    }
}