package com.example.week6

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Bisa pakai viewModels biasa karena tidak perlu parameter khusus
import androidx.navigation.fragment.findNavController
import com.example.week6.databinding.FragmentAddCommunityBinding // Ganti dengan nama layout Anda

class AddCommunityFragment : Fragment() {

    private var _binding: FragmentAddCommunityBinding? = null
    private val binding get() = _binding!!

    // ViewModel biasa karena tidak perlu parameter userId
    private val addCommunityViewModel: AddCommunityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.buttonCreateCommunity.setOnClickListener {
            val name = binding.editTextCommunityName.text.toString()
            addCommunityViewModel.createCommunity(name)
        }

        // Hapus error saat user mengetik
        binding.editTextCommunityName.addTextChangedListener {
            binding.textFieldCommunityName.error = null
        }

        // Listener untuk tombol back
        binding.buttonBackAddCommunity.setOnClickListener { // Ganti ID jika perlu
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        // Observe error validasi
        addCommunityViewModel.validationError.observe(viewLifecycleOwner) { error ->
            binding.textFieldCommunityName.error = error // Tampilkan error di text field
        }

        // Observe event community berhasil dibuat
        addCommunityViewModel.communityCreatedEvent.observe(viewLifecycleOwner) { created ->
            if (created) {
                Toast.makeText(context, "Community created successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Kembali ke AddPostFragment
                addCommunityViewModel.onCommunityCreatedEventDone() // Reset event
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}