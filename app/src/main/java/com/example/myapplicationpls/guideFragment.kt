package com.example.myapplicationpls

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback

class guideFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_guide, container, false)
        val videoSpinner = view.findViewById<Spinner>(R.id.ytspinner)
        val youtubePlayerView = view.findViewById<YouTubePlayerView>(R.id.youtubePlayer)
        val videoOptions = resources.getStringArray(R.array.video_options)
        val videoLinks = resources.getStringArray(R.array.video_links)

        val tittle = view.findViewById<TextView>(R.id.titleExercise)
        val description = view.findViewById<TextView>(R.id.descriptionExercise)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, videoOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        videoSpinner.adapter = adapter


        videoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedVideoLink = videoLinks[position]
                val ytSelected = videoSpinner.selectedItem.toString()
                when (ytSelected) {
                    "Push-Up" -> {
                        tittle.text = "Push-Up"
                        description.setText(R.string.pushup)
                    }
                    "Legs Barbell Squat" -> {
                        tittle.text = "Legs Barbell Squat"
                        description.setText(R.string.LegsBarbellSquat)
                    }
                    "Legs Hack Squat" -> {
                        tittle.text = "Legs Hack Squat"
                        description.setText(R.string.LegsHackSquat)
                    }
                    "Legs Hamstring Curls" -> {
                        tittle.text = "Legs Hamstring Curls"
                        description.setText(R.string.LegsHamstringCurls)
                    }
                    "Legs Quad Extensions" -> {
                        tittle.text = "Legs Quad Extensions"
                        description.setText(R.string.LegsQuadExtensions)
                    }
                    "Biceps Drag Curl" -> {
                        tittle.text = "Biceps Drag Curls"
                        description.setText(R.string.BicepsDragCurl)
                    }
                    "Biceps Dumbbell Curl" -> {
                        tittle.text = "Biceps Dumbbell Curl"
                        description.setText(R.string.BicepsDumbbellCurl)
                    }
                    "Biceps EZ Barbell Curl" -> {
                        tittle.text = "Biceps EZ Barbell Curl"
                        description.setText(R.string.BicepsEZBarbellCurl)
                    }
                    else -> {
                    }
                }
                val videoId = selectedVideoLink
                youtubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                })
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        return view
    }
}
