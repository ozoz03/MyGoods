package com.oz.mygoods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.oz.mygoods.good.Good
import com.oz.mygoods.good.GoodService
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        val listItems: List<String> = mutableListOf()
        val goodAdapter =
            ArrayAdapter(this.requireContext(), android.R.layout.simple_list_item_1, listItems)
        val listView = view.findViewById<ListView>(R.id.order_list_view)
        listView.adapter = goodAdapter
        view.findViewById<Button>(R.id.button_load).setOnClickListener {
            val createCall = service.needed()
            createCall.enqueue(object : Callback<List<Good>> {
                override fun onResponse(
                    call: Call<List<Good>>, response: Response<List<Good>>
                ) {
                    (listItems as MutableList).clear()
                    for (item in response.body()) {
                        listItems.add(item.toString())
                    }
                    (listView.adapter as BaseAdapter).notifyDataSetChanged()
                }

                override fun onFailure(
                    call: Call<List<Good>>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                }
            })
        }
    }
}