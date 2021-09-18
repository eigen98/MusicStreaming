package com.example.musicstreaming

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.musicstreaming.databinding.FragmentPlayerBinding
import com.example.musicstreaming.service.MusicDto
import com.example.musicstreaming.service.MusicEntity
import com.example.musicstreaming.service.MusicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlayerFragment : Fragment(R.layout.fragment_player){


    private var binding : FragmentPlayerBinding? = null
    private var isWatchingPlayListView = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)
        binding = fragmentPlayerBinding

        initPlayListButton(fragmentPlayerBinding)
        getVideoListFromServer()
    }

    private fun initPlayListButton(fragmentPlayerBinding: FragmentPlayerBinding) {

        fragmentPlayerBinding.playlistImageView.setOnClickListener{
            //만약 서버에서 데이터가 다 불려오지 않은 상황일 때 예외처리 필요
            fragmentPlayerBinding.playerViewGroup.isVisible = isWatchingPlayListView

            fragmentPlayerBinding.playListViewGroup.isVisible = isWatchingPlayListView.not()

            isWatchingPlayListView = !isWatchingPlayListView
        }
    }


    private fun getVideoListFromServer(){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MusicService::class.java)
            .also {
                it.listMusics() //만들어둔 리스트불러오기 함수를 호출하고 콜을 반환하여 enqueue를 통해 오브젝트콜백으로 가져옴
                    .enqueue(object : Callback<MusicDto>{
                        override fun onResponse(
                            call: Call<MusicDto>,
                            response: Response<MusicDto>
                        ) {
                            Log.d("PlayerFragment", "${response.body()}")
//                            MusicEntity확장
                            response.body()?.let {
                                it.musics.mapIndexed{index, musicEntity ->
                                    musicEntity.mapper(index.toLong())
                                }
                            }
                        }

                        override fun onFailure(call: Call<MusicDto>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
            }
    }




    companion object {
        fun newInstance(): PlayerFragment{
            return PlayerFragment()
        }
    }
}