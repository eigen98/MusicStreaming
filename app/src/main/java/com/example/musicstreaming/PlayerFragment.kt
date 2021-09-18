package com.example.musicstreaming

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicstreaming.databinding.FragmentPlayerBinding
import com.example.musicstreaming.service.MusicDto
import com.example.musicstreaming.service.MusicEntity
import com.example.musicstreaming.service.MusicService
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlayerFragment : Fragment(R.layout.fragment_player){


    private var binding : FragmentPlayerBinding? = null
    private var isWatchingPlayListView = true
    private lateinit var playListAdapter: PlayListAdapter

    private var player : SimpleExoPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)
        binding = fragmentPlayerBinding

        initPlayView(fragmentPlayerBinding)
        initPlayListButton(fragmentPlayerBinding)
        initPlayControlButtons(fragmentPlayerBinding)
        initRecyclerView(fragmentPlayerBinding)

        getVideoListFromServer()
    }

    private fun initPlayControlButtons(fragmentPlayerBinding: FragmentPlayerBinding){
        fragmentPlayerBinding.playControlImageView.setOnClickListener {
            val player = this.player ?: return@setOnClickListener

            if(player.isPlaying){
                player.pause()
            }else{
                player.play()
            }
        }
        fragmentPlayerBinding.skipNextImageView.setOnClickListener {

        }
        fragmentPlayerBinding.skipPrevImageView.setOnClickListener {

        }

    }

    private fun initPlayView(fragmentPlayerBinding : FragmentPlayerBinding){
        context?.let{
            player = SimpleExoPlayer.Builder(it).build()
        }


        fragmentPlayerBinding.playerView.player = player
        //플레이어에 리스너 걸기
        binding?.let { binding ->
            player?.addListener(object : Player.EventListener{
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    //플레이어가 재생이될 때 일시정지가 될 때 콜백으로 내려오는 함수
                    if(isPlaying){
                        binding.playControlImageView.setImageResource(R.drawable.ic_baseline_pause_48)

                    }else{
                        binding.playControlImageView.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }

                }
            })

        }
    }

    private fun initRecyclerView(fragmentPlayerBinding : FragmentPlayerBinding){
        playListAdapter = PlayListAdapter {
            //아이템 눌렸을 때 음악을 재생
        }

        fragmentPlayerBinding.playListRecyclerView.apply {
            adapter = playListAdapter
            layoutManager = LinearLayoutManager(context)
        }
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
                                val modelList = it.musics.mapIndexed{index, musicEntity ->
                                    musicEntity.mapper(index.toLong())
                                }

                                setMusicList(modelList)
                                playListAdapter.submitList(modelList)
                            }
                        }

                        override fun onFailure(call: Call<MusicDto>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
            }
    }

    private fun setMusicList(modelList: List<MusicModel>) {
        context?.let{
            player?.addMediaItems(modelList.map{musicModel ->
                MediaItem.Builder()
                    .setMediaId(musicModel.id.toString())
                    .setUri(musicModel.streamUrl)
                    .build()
            })

            player?.prepare()
            player?.play()
        }

    }


    companion object {
        fun newInstance(): PlayerFragment{
            return PlayerFragment()
        }
    }
}