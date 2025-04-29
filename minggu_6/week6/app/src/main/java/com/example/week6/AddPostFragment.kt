package com.example.week6

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.week6.databinding.FragmentAddPostBinding // Ganti dengan nama layout Anda

class AddPostFragment : Fragment() {

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var addPostViewModel: AddPostViewModel

    private var communityList: List<Community> = emptyList()
    private var selectedCommunity: Community? = null
    private var currentUserId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.loggedInUserId.observe(viewLifecycleOwner) { userId ->
            if (userId != null) {
                if (currentUserId == null || currentUserId != userId) { // Re-init jika user berubah
                    currentUserId = userId
                    setupViewModel(userId)
                    setupUIListeners()
                    observeViewModel()
                }
            } else {
                // Kembali ke login jika user ID tidak valid
                if (findNavController().currentDestination?.id == R.id.addPostFragment) {
                    findNavController().popBackStack(R.id.homeFragment, false)
                    findNavController().navigate(R.id.action_global_loginFragment)
                }
            }
        }
    }

    private fun setupViewModel(userId: Int) {
        val factory = AddPostViewModel.Factory(requireActivity().application, userId)
        addPostViewModel = ViewModelProvider(this, factory)[AddPostViewModel::class.java]
    }

    private fun setupUIListeners() {
        binding.buttonPost.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val description = binding.editTextDescription.text.toString()
            // Pastikan selectedCommunity sudah dipilih
            selectedCommunity?.let { community ->
                addPostViewModel.createPost(title, description, community)
            } ?: run {
                // Tampilkan error jika komunitas belum dipilih
                binding.textFieldCommunity.error = "Please select a community"
                Toast.makeText(context, "Please select a community", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewCreateCommunity.setOnClickListener {
            findNavController().navigate(R.id.action_addPostFragment_to_addCommunityFragment)
        }

        binding.buttonBackAddPost.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCommunityDropdown(communities: List<Community>) {
        communityList = communities
        val communityNames = communities.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, communityNames)
        val autoCompleteTextView = (binding.textFieldCommunity.editText as? AutoCompleteTextView)
        autoCompleteTextView?.setAdapter(adapter)

        // Mengganti parameter yang tidak dipakai dengan underscore (_)
        autoCompleteTextView?.setOnItemClickListener { _, _, position, _ ->
            selectedCommunity = communityList.find { it.name == adapter.getItem(position) }
            binding.textFieldCommunity.error = null // Hapus error saat item dipilih
        }

        // Clear selection jika text dikosongkan
        autoCompleteTextView?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    selectedCommunity = null
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun observeViewModel() {
        addPostViewModel.allCommunities.observe(viewLifecycleOwner) { communities ->
            setupCommunityDropdown(communities ?: emptyList())
            // Set teks awal jika ada selectedCommunity sebelumnya (misal setelah rotasi)
            selectedCommunity?.let {
                (binding.textFieldCommunity.editText as? AutoCompleteTextView)?.setText(it.name, false)
            }
        }

        addPostViewModel.validationError.observe(viewLifecycleOwner) { error ->
            when {
                error?.contains("community", ignoreCase = true) == true -> binding.textFieldCommunity.error = error
                error?.contains("title", ignoreCase = true) == true -> binding.textFieldTitle.error = error
                error?.contains("description", ignoreCase = true) == true -> binding.textFieldDescription.error = error
                error != null -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                else -> {
                    binding.textFieldCommunity.error = null
                    binding.textFieldTitle.error = null
                    binding.textFieldDescription.error = null
                }
            }
        }

        addPostViewModel.postCreatedEvent.observe(viewLifecycleOwner) { created ->
            if (created) {
                Toast.makeText(context, "Post created successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                addPostViewModel.onPostCreatedEventDone()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}