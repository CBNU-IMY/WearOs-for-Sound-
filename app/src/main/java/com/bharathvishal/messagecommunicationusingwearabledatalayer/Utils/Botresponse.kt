package com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils

import android.util.Log
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Api.RetrofitBuilder
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Dto.CorpusDto
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Dto.CorpusDto2
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils.Constants.OPEN_GOOGLE
import com.bharathvishal.messagecommunicationusingwearabledatalayer.Utils.Constants.OPEN_SEARCH
/*
import com.codepalace.chatbot.Api.CorpusApi
import com.codepalace.chatbot.Api.RetrofitBuilder
import com.codepalace.chatbot.Dto.CorpusDto
import com.codepalace.chatbot.Dto.CorpusDto2
import com.codepalace.chatbot.utils.Constants.OPEN_GOOGLE
import com.codepalace.chatbot.utils.Constants.OPEN_SEARCH */

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import kotlinx.coroutines.*

object BotResponse {

    fun basicResponses(_message: String, _corpuslist: List<CorpusDto>): String {

        val random = (0..2).random()
        val message =_message.toLowerCase()
        var corpuslist=_corpuslist

        return when {

            //Flips a coin
            message.contains("flip") && message.contains("coin") -> {
                val r = (0..1).random()
                val result = if (r == 0) "heads" else "tails"

                "I flipped a coin and it landed on $result"
            }

            //Math calculations
            message.contains("solve") -> {
                val equation: String? = message.substringAfterLast("solve")
                return try {
                    val answer = SolveMath.solveMath(equation ?: "0")
                    "$answer"

                } catch (e: Exception) {
                    "Sorry, I can't solve that."
                }
            }

            //Hello
            message.contains("hello") -> {
                println("corpuslist mansinn = ${corpuslist}")

                // println("second name = ${name}")
                corpuslist.get(2).system_response1
                /*  when (random) {
                    //  0 -> sexmessage
                     /* 1 -> "Sup"
                      2 -> "Buongiorno!"*/
                    //  else -> "error" }*/
            }

            //How are you?
            message.contains("how are you") -> {
                when (random) {
                    0 -> "I'm doing fine, thanks!"
                    1 -> "I'm hungry..."
                    2 -> "Pretty good! How about you?"
                    else -> "error"
                }
            }

            //What time is it?
            message.contains("time") && message.contains("?")-> {
                val timeStamp = Timestamp(System.currentTimeMillis())
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
                val date = sdf.format(Date(timeStamp.time))

                date.toString()
            }

            //Open Google
            message.contains("open") && message.contains("google")-> {
                OPEN_GOOGLE
            }

            //Search on the internet
            message.contains("search")-> {
                OPEN_SEARCH
            }

            //When the programme doesn't understand...
            else -> {
                when (random) {
                    0 -> "?????? ????????? ??? ????????????..."
                    1 -> "?????? ?????? ??? ??? ??????????????"
                    2 -> "????????? ????????"
                    else -> "error"
                }
            }
        }
    }


    fun Corpuslist2(_corpuslist : CorpusDto){
        var corpuslist=_corpuslist

        val call = RetrofitBuilder.corpusapi.getAllByMaincategoryResponse("??????")
        var corpus = CorpusDto2("test")
        corpus.system_response1="test2"
        println("corpus = ${corpus}")

        Thread{
            call.enqueue(object : Callback<List<CorpusDto>> { // ????????? ?????? ?????? ?????????
                override fun onResponse( // ????????? ????????? ??????
                    call: Call<List<CorpusDto>>,
                    response: Response<List<CorpusDto>>
                ) {
                    if(response.isSuccessful()){ // ?????? ??? ?????? ??????
                        corpuslist= response.body()!!.get(0)
                        println("corpuslist = ${corpuslist}")
                        println("response.body() = ${response.body()}")
                        corpus.system_response1=response.body()?.get(0)!!.system_response1
                        println("second corpus = ${corpus}")
                    }else{
                        // ?????? ?????? but ?????? ??????
                        Log.d("RESPONSE", "FAILURE")
                    }

                }

                override fun onFailure(call: Call<List<CorpusDto>>, t: Throwable) {
                    // ????????? ????????? ??????
                    Log.d("CONNECTION FAILURE: ", t.localizedMessage)
                }
            })
        }.start()

        try{
            Thread.sleep(50)
        } catch(e: Exception){
            e.printStackTrace()
        }

        println("third corpus = ${corpus}")

    }



}