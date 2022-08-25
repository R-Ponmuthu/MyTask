package com.example.task.ui.picture.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.task.R
import com.example.task.databinding.PictureFragmentBinding
import com.example.task.databinding.PictureListFragmentBinding


class PictureFragment : Fragment() {

    private val args: PictureFragmentArgs by navArgs()
    private lateinit var binding: PictureFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PictureFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide
            .with(context!!)
            .load(args.picture.path)
            .centerCrop()
            .into(binding.imageView)

    }
}