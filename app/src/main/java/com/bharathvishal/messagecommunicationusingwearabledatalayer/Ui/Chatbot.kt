package com.bharathvishal.messagecommunicationusingwearabledatalayer.Ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Api.RetrofitBuilder
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Data.Message
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Dto.ChatbotDto
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Dto.CorpusDto
import com.bharathvishal.messagecommunicationusingwearabledatalayer.R
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils.BotResponseAccept
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils.BotResponseBargain
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils.BotResponseRefuse
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils.Constants.OPEN_GOOGLE
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils.Constants.OPEN_SEARCH
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils.Constants.RECEIVE_ID
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils.Constants.SEND_ID
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils.Time
import com.bharathvishal.messagecommunicationusingwearabledatalayer.databinding.ActivityChatbotBinding
import kotlinx.android.synthetic.main.activity_chatbot.*

/*
import com.codepalace.chatbot.Api.RetrofitBuilder
import com.codepalace.chatbot.Data.Message
import com.codepalace.chatbot.Dto.ChatbotDto
import com.codepalace.chatbot.Dto.CorpusDto
import com.codepalace.chatbot.Dto.CorpusDto2
import com.codepalace.chatbot.databinding.ActivityChatbotBinding
import com.codepalace.chatbot.utils.*
import com.codepalace.chatbot.utils.Constants.RECEIVE_ID
import com.codepalace.chatbot.utils.Constants.SEND_ID
import com.codepalace.chatbot.utils.Constants.OPEN_GOOGLE
import com.codepalace.chatbot.utils.Constants.OPEN_SEARCH
import kotlinx.android.synthetic.main.activity_chatbot.*  */

import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

private lateinit var binding: ActivityChatbotBinding

class Chatbot : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var speechRecognizer: SpeechRecognizer
    private var textToSpeech: TextToSpeech? = null

    lateinit var soundPool: SoundPool
    private var RefuseSoundList: List<Int>?  = null
    private var TreSoundList: List<Int>?  = null
    private var TTreSoundList: List<Int>?  = null

    //You can ignore this messageList if you're coming from the tutorial,
    // it was used only for my personal debugging
    var messagesList = mutableListOf<Message>()

    private lateinit var adapter: MessagingAdapter
    //private val botList = listOf("상우야", "정주야", "승규야")
    private var corpuslist : List<CorpusDto> = listOf() //corpus 데이터 받을 리스트 변수
    private lateinit var stage : String      //register로 부터 입력받은 우울증 단계
    private var chatresponse=""   //ai chatbot 답변

    override fun onCreate(savedInstanceState: Bundle?) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            var audioAttrs: AudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttrs)
                .build()
        }
        RefuseSoundList = listOf(
            soundPool.load(this, R.raw.re0, 1),
            soundPool.load(this, R.raw.re1, 1),
            soundPool.load(this, R.raw.re2, 1),
            soundPool.load(this, R.raw.re3, 1),
            soundPool.load(this, R.raw.re4, 1),
            soundPool.load(this, R.raw.re5, 1),
            soundPool.load(this, R.raw.re6, 1),
            soundPool.load(this, R.raw.re7, 1),
            soundPool.load(this, R.raw.re8, 1),
            )

        TreSoundList = listOf(
            soundPool.load(this, R.raw.tre1, 1),
            soundPool.load(this, R.raw.tre2, 1),
            soundPool.load(this, R.raw.tre3, 1),
            soundPool.load(this, R.raw.tre4, 1),
            soundPool.load(this, R.raw.tre5, 1),
            soundPool.load(this, R.raw.tre6, 1),
            soundPool.load(this, R.raw.tre7, 1),
            soundPool.load(this, R.raw.tre8, 1),
            soundPool.load(this, R.raw.tre9, 1),
            soundPool.load(this, R.raw.tre10, 1),
            soundPool.load(this, R.raw.tre11, 1),
            )
        TTreSoundList = listOf(
            soundPool.load(this, R.raw.ttre1, 1),
            soundPool.load(this, R.raw.ttre2, 1),
            soundPool.load(this, R.raw.ttre3, 1),
            soundPool.load(this, R.raw.ttre4, 1),
            soundPool.load(this, R.raw.ttre5, 1),
            soundPool.load(this, R.raw.ttre6, 1),
            soundPool.load(this, R.raw.ttre7, 1),
            soundPool.load(this, R.raw.ttre8, 1),
            soundPool.load(this, R.raw.ttre9, 1),
            )
        stage=intent.getStringExtra("stage")!!

        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        Corpuslist()

        // 권한 설정
        requestPermission()

        setAlarm()

        // RecognizerIntent 생성
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)    // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")         // 언어 설정

        // <말하기> 버튼 눌러서 음성인식 시작
        binding.btnSpeech.setOnClickListener {
            // 새 SpeechRecognizer 를 만드는 팩토리 메서드
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@Chatbot)
            speechRecognizer.setRecognitionListener(recognitionListener)    // 리스너 설정
            speechRecognizer.startListening(intent)                         // 듣기 시작
        }

        recyclerView()

        clickEvents()

        val random = (0..2).random()
        if(stage.equals("refuse")){
            customBotMessage("안녕! , 오늘 기분은 어때?")
            GlobalScope.launch {
                delay(1000)

                withContext(Dispatchers.Main) {
                    rv_messages.scrollToPosition(adapter.itemCount - 1)
                    soundPool.play(RefuseSoundList!!.get(0),2f, 2f,0,0,1f)

                }
            }
        }

    }

    private fun setAlarm() {
        textToSpeech = TextToSpeech(this@Chatbot, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                val result = textToSpeech!!.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS","해당언어는 지원되지 않습니다.")
                    return@OnInitListener
                }
            }
        })
    }

    // 권한 설정 메소드
    private fun requestPermission() {
        // 버전 체크, 권한 허용했는지 체크
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(this@Chatbot, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@Chatbot,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }

    // 리스너 설정
    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        // 말하기 시작할 준비가되면 호출
        override fun onReadyForSpeech(params: Bundle) {
            Toast.makeText(applicationContext, "음성인식 시작", Toast.LENGTH_SHORT).show()
            binding.tvState.text = "이제 말씀하세요!"
        }
        // 말하기 시작했을 때 호출
        override fun onBeginningOfSpeech() {
            binding.tvState.text = "잘 듣고 있어요."
        }
        // 입력받는 소리의 크기를 알려줌
        override fun onRmsChanged(rmsdB: Float) {}
        // 말을 시작하고 인식이 된 단어를 buffer에 담음
        override fun onBufferReceived(buffer: ByteArray) {}
        // 말하기를 중지하면 호출
        override fun onEndOfSpeech() {
            binding.tvState.text = "끝!"
        }
        // 오류 발생했을 때 호출
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                else -> "알 수 없는 오류임"
            }
            binding.tvState.text = "에러 발생: $message"
        }
        // 인식 결과가 준비되면 호출
        override fun onResults(results: Bundle) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줌
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            for (i in matches!!.indices) binding.etMessage.setText(matches[i])
            sendMessage()

        }
        // 부분 인식 결과를 사용할 수 있을 때 호출
        override fun onPartialResults(partialResults: Bundle) {}
        // 향후 이벤트를 추가하기 위해 예약
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    private fun clickEvents() {

        //Send a message
        btn_send.setOnClickListener {
            sendMessage()
        }

        //Scroll back to correct position when user clicks on text view
        et_message.setOnClickListener {
            GlobalScope.launch {
                delay(100)

                withContext(Dispatchers.Main) {
                    rv_messages.scrollToPosition(adapter.itemCount - 1)

                }
            }
        }
    }

    private fun recyclerView() {
        adapter = MessagingAdapter()
        rv_messages.adapter = adapter
        rv_messages.layoutManager = LinearLayoutManager(applicationContext)

    }

    override fun onStart() {
        super.onStart()
        //In case there are messages, scroll to bottom when re-opening app
        GlobalScope.launch {
            delay(100)
            withContext(Dispatchers.Main) {
                rv_messages.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    private fun sendMessage() {
        val message = et_message.text.toString()
        val timeStamp = Time.timeStamp()

        if (message.isNotEmpty()) {
            //Adds it to our local list
            messagesList.add(Message(message, SEND_ID, timeStamp))
            et_message.setText("")

            adapter.insertMessage(Message(message, SEND_ID, timeStamp))
            rv_messages.scrollToPosition(adapter.itemCount - 1)

            botResponse(message)

        }
    }

    private fun botResponse(message: String) {

        val timeStamp = Time.timeStamp()

        GlobalScope.launch {
            //Fake response delay
            delay(1000)
            var response=""

            withContext(Dispatchers.Main) {

                //Gets the response(3 case)
                if(stage.equals("refuse")) {
                    response = BotResponseRefuse.basicResponses(message, corpuslist)
                    if(response == "반가워 나를 친구처럼 대해줄래?"){
                        soundPool.play(RefuseSoundList!!.get(1),2f, 2f,0,0,1f)
                    }
                    else if(response == "그냥 일상 이야기든 고민이든 너가 하고싶은 이야기를 하면 돼"){
                        soundPool.play(RefuseSoundList!!.get(2),2f, 2f,0,0,1f)
                    }
                    else if(response == "그렇게 생각해주니 고마워"){
                        soundPool.play(RefuseSoundList!!.get(3),2f, 2f,0,0,1f)
                    }
                    else if(response == "응 다음에 또 보자"){
                        soundPool.play(RefuseSoundList!!.get(4),2f, 2f,0,0,1f)
                    }
                    else if(response == "별말씀을요 기분이 풀리셨다면 다행이에요"){
                        soundPool.play(RefuseSoundList!!.get(5),2f, 2f,0,0,1f)

                    }
                    else if(response == "무슨 말인지 잘 모르겠어..."){
                        soundPool.play(RefuseSoundList!!.get(6),2f, 2f,0,0,1f)
                    }
                    else if(response == "미안 다시 한 번 말해줄래??"){
                        soundPool.play(RefuseSoundList!!.get(7),2f, 2f,0,0,1f)
                    }
                    else if(response == "뭐라고 했지??"){
                        soundPool.play(RefuseSoundList!!.get(8),2f, 2f,0,0,1f)
                    }
                    //tre
                    else if(response == "더 울고 싶으시면 말씀하세요"){
                        soundPool.play(TreSoundList!!.get(0),2f, 2f,0,0,1f)
                    }
                    else if(response == "신경쓰지 마세요"){
                        soundPool.play(TreSoundList!!.get(1),2f, 2f,0,0,1f)
                    }
                    else if(response == "오늘은 쉬세요"){
                        soundPool.play(TreSoundList!!.get(2),2f, 2f,0,0,1f)
                    }
                    else if(response == "정말 큰일이었네요"){
                        soundPool.play(TreSoundList!!.get(3),2f, 2f,0,0,1f)
                    }
                    else if(response == "안녕 안녕 안녕"){
                        soundPool.play(TreSoundList!!.get(6),2f, 2f,0,0,1f)
                    }
                    else if(response == "그게 최선이었을 거예요"){
                        soundPool.play(TreSoundList!!.get(7),2f, 2f,0,0,1f)
                    }
                    else if(response == "저도 좋아요"){
                        soundPool.play(TreSoundList!!.get(8),2f, 2f,0,0,1f)
                    }
                    else if(response == "지금보다 더 잘 살 거예요"){
                        soundPool.play(TreSoundList!!.get(9),2f, 2f,0,0,1f)
                    }
                    else if(response == "공부는 끝이 없죠"){
                        soundPool.play(TreSoundList!!.get(10),2f, 2f,0,0,1f)
                    }
                    else if(response == "더 울고 싶으시면 말씀하세요"){
                        soundPool.play(TreSoundList!!.get(0),2f, 2f,0,0,1f)
                    }
                    //chat
                    else if(response == "무슨 일 이 있나요"){
                        soundPool.play(TTreSoundList!!.get(0),2f, 2f,0,0,1f)
                    }
                    else if(response == "힘내세요 좋은날이 올 거에요"){
                        soundPool.play(TTreSoundList!!.get(1),2f, 2f,0,0,1f)
                    }
                    else if(response == "우리 천천히 차근차근 해보아요"){
                        soundPool.play(TTreSoundList!!.get(2),2f, 2f,0,0,1f)
                    }
                    else if(response == "스스로를 믿어 보세요, 좋은날이 올거에요"){
                        soundPool.play(TTreSoundList!!.get(4),2f, 2f,0,0,1f)
                    }
                    else if(response == "잘 할 수 있을 거에요 화이팅"){
                        soundPool.play(TTreSoundList!!.get(6),2f, 2f,0,0,1f)
                        GlobalScope.launch {
                            delay(2000)
                            withContext(Dispatchers.Main) {
                                rv_messages.scrollToPosition(adapter.itemCount - 1)
                                soundPool.play(TTreSoundList!!.get(7),2f, 2f,0,0,1f)

                            }
                        }

                    }

                }
                else if(stage.equals("bargain")){
                    response = BotResponseBargain.basicResponses(message, corpuslist)
                }
                else{
                    response = BotResponseAccept.basicResponses(message, corpuslist)
                }
                //Adds it to our local list
                messagesList.add(Message(response, RECEIVE_ID, timeStamp))

                //Inserts our message into the adapter
                //adapter.insertMessage(Message(response, RECEIVE_ID, timeStamp))
                adapter.insertMessage(Message(response, RECEIVE_ID, timeStamp))
                //Scrolls us to the position of the latest message
                rv_messages.scrollToPosition(adapter.itemCount - 1)

                //Starts Google
                when (response) {
                    OPEN_GOOGLE -> {
                        val site = Intent(Intent.ACTION_VIEW)
                        site.data = Uri.parse("https://www.google.com/")
                        startActivity(site)
                    }
                    OPEN_SEARCH -> {
                        val site = Intent(Intent.ACTION_VIEW)
                        val searchTerm: String? = message.substringAfterLast("search")
                        site.data = Uri.parse("https://www.google.com/search?&q=$searchTerm")
                        startActivity(site)
                    }

                }

                //textToSpeech?.speak(response, TextToSpeech.QUEUE_FLUSH, null)
                textToSpeech?.playSilentUtterance(750,TextToSpeech.QUEUE_ADD,null) // deley시간 설정
            }
        }
    }

    private fun customBotMessage(message: String) {

        GlobalScope.launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                val timeStamp = Time.timeStamp()
                messagesList.add(Message(message, RECEIVE_ID, timeStamp))
                adapter.insertMessage(Message(message, RECEIVE_ID, timeStamp))

                rv_messages.scrollToPosition(adapter.itemCount - 1)

                //textToSpeech?.speak(message, TextToSpeech.QUEUE_FLUSH, null)
                //textToSpeech?.playSilentUtterance(750,TextToSpeech.QUEUE_ADD,null) // deley시간 설정
            }
        }
    }



    fun Corpuslist() {
        lateinit var call : Call<List<CorpusDto>>
        if(stage.equals("refuse")){
            call = RetrofitBuilder.corpusapi.getAllByMaincategoryResponse("분노")
        }
        else if(stage.equals("bargain")){
            call=RetrofitBuilder.corpusapi.getAllbyStatuskeywordResponse("연애, 결혼, 출산")
        }
        else{
            call = RetrofitBuilder.corpusapi.getAllByMaincategoryResponse("슬픔")
        }


        Thread{
            call.enqueue(object : Callback<List<CorpusDto>> { // 비동기 방식 통신 메소드
                override fun onResponse( // 통신에 성공한 경우
                    call: Call<List<CorpusDto>>,
                    response: Response<List<CorpusDto>>
                ) {
                    if(response.isSuccessful()){ // 응답 잘 받은 경우
                        corpuslist= response.body()!!
                        println("corpuslist = ${corpuslist}")
                    }else{
                        // 통신 성공 but 응답 실패
                        Log.d("RESPONSE", "FAILURE")
                    }

                }

                override fun onFailure(call: Call<List<CorpusDto>>, t: Throwable) {
                    // 통신에 실패한 경우
                    Log.d("CONNECTION FAILURE: ", t.localizedMessage)
                }
            })
        }.start()

        try{
            Thread.sleep(50)
        } catch(e: Exception){
            e.printStackTrace()
        }

    }


    private suspend fun Chatbotlist(s: String) {
        withContext(Dispatchers.IO) {

            runCatching {
                val retrofit = RetrofitBuilder.chatbotapi.getKogpt2Response(s)
                val res = retrofit.execute().body()
                //res.code() == 200
                println("res = ${res}")
                chatresponse= res!!.answer
            }.getOrDefault(false)
        }

    }



}