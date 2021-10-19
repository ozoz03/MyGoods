package com.oz.mygoods

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckedTextView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.oz.mygoods.good.Good
import com.oz.mygoods.good.GoodService
import net.openid.appauth.AuthState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    var retrofit = Retrofit.Builder()
//        .baseUrl("http://192.168.43.81:5000/")
        .baseUrl("https://my-goods.herokuapp.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: GoodService = retrofit.create(GoodService::class.java)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }


    fun readAuthState(): AuthState {
        val authPrefs = context?.getSharedPreferences("OktaAppAuthState", Context.MODE_PRIVATE)
        val stateJson = authPrefs?.getString("state", "")
        return if (!stateJson!!.isEmpty()) {
            try {
                AuthState.jsonDeserialize(stateJson)
            } catch (exp: org.json.JSONException) {
                Log.e("ERROR",exp.message)
                AuthState()
            }

        } else {
            AuthState()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<Button>(R.id.button_next).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        val goods: List<Good> = mutableListOf()
        val goodAdapter =
            ArrayAdapter(this.requireContext(), android.R.layout.simple_list_item_checked, goods)
        val listView = view.findViewById<ListView>(R.id.order_list_view)
        listView.adapter = goodAdapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
//                Log.i(TAG, "onItemClick: $position")
                val v = view as CheckedTextView
                val currentCheck = v.isChecked
                val good: Good = listView.getItemAtPosition(position) as Good
                good.needed = (!currentCheck)
            }

        view.findViewById<Button>(R.id.button_load).setOnClickListener {
            // notification
                view ->
            Snackbar.make(view, "Loading actual list of needed goods", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            val createCall = readAuthState().accessToken?.let { service.needed(it) }
            createCall?.enqueue(object : Callback<List<Good>> {
                override fun onResponse(
                    call: Call<List<Good>>, response: Response<List<Good>>
                ) {
                    (goods as MutableList).clear()
                    for ((i, item) in response.body().withIndex()) {
                        goods.add(item)
                        listView.setItemChecked(i, item.needed?.not() ?: false)
                    }
                    (listView.adapter as BaseAdapter).notifyDataSetChanged()
                }

                override fun onFailure(
                    call: Call<List<Good>>,
                    t: Throwable
                ) {
                    (goods as MutableList).clear()
                    goods.add(Good(name = "Error:${t.message}"))
                    goods.add(Good(name = "Try again"))
                    (listView.adapter as BaseAdapter).notifyDataSetChanged()
                    t.printStackTrace()
                }
            })
        }

        view.findViewById<Button>(R.id.button_sync).setOnClickListener {
            // notification
                view ->
            Snackbar.make(view, "Synchronization list of needed goods", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            for (i in 0 until listView.getCount()) {
                val good = (listView.getItemAtPosition(i) as Good)
                if (good.needed!!.not()) {
                    val updateCall = service.update(good.id, good)
                    updateCall.enqueue(object : Callback<Good?> {
                        override fun onResponse(
                            call: Call<Good?>, response: Response<Good?>
                        ) {
                            val updatedGood = response.body()
                            Snackbar.make(view, "Update the good: $updatedGood", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show()
                        }

                        override fun onFailure(
                            call: Call<Good?>,
                            t: Throwable
                        ) {
                            t.printStackTrace()
                        }
                    })
                }
            }
        }
    }
}