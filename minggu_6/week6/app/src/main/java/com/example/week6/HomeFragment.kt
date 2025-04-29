package com.example.week6

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
// --- PASTIKAN BINDING BENAR ---
import com.example.week6.databinding.FragmentHomeBinding // Menggunakan binding untuk layout home Reddid

// ViewModel dan Adapter untuk Reddid
// (HomeViewModel dan PostAdapter sudah kita buat sebelumnya)

class HomeFragment : Fragment() {

    // --- GUNAKAN BINDING YANG BENAR ---
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel untuk status login/logout
    private val authViewModel: AuthViewModel by activityViewModels()
    // ViewModel untuk data Home (posts, search, vote)
    private lateinit var homeViewModel: HomeViewModel
    // Adapter untuk RecyclerView posts
    private lateinit var postAdapter: PostAdapter

    private var currentUserId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout home Reddid yang benar
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Amati ID user yang login
        authViewModel.loggedInUserId.observe(viewLifecycleOwner) { userId ->
            if (userId == null) {
                Log.d("HomeFragment", "User ID null, navigating to login")
                if (findNavController().currentDestination?.id == R.id.homeFragment) {
                    findNavController().navigate(R.id.action_global_loginFragment) // Kembali ke Login
                }
            } else {
                Log.d("HomeFragment", "User ID: $userId")
                // Inisialisasi ViewModel dan UI HANYA jika user ID valid dan belum diinisialisasi
                // atau jika user ID berubah (misal kasus multi-login di masa depan)
                if (currentUserId == null || currentUserId != userId) {
                    currentUserId = userId
                    setupViewModel(userId)
                    setupRecyclerView() // Setup RecyclerView setelah ViewModel siap
                    setupUIListeners()
                    observeViewModel() // Mulai mengamati data
                }
            }
        }
    }

    private fun setupViewModel(userId: Int) {
        // Gunakan Factory untuk HomeViewModel Reddid
        val factory = HomeViewModel.Factory(requireActivity().application, userId)
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // Pastikan userId tidak null saat membuat adapter
        currentUserId?.let { userId ->
            // Gunakan PostAdapter untuk Reddid
            postAdapter = PostAdapter(
                onPostClick = { post ->
                    // Navigasi ke DetailPostFragment
                    val action = HomeFragmentDirections.actionHomeFragmentToDetailPostFragment(post.post.id)
                    findNavController().navigate(action)
                },
                onVoteClick = { postId, voteAction ->
                    // Handle vote via ViewModel
                    homeViewModel.handleVote(postId, voteAction)
                },
                currentUserId = userId // Berikan userId ke adapter
            )

            // Set adapter ke RecyclerView di layout fragment_home.xml
            binding.recyclerViewPosts.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = postAdapter
            }
        } ?: Log.e("HomeFragment", "Cannot setup RecyclerView, currentUserId is null during setupRecyclerView")
    }

    private fun setupUIListeners() {
        // Listener untuk Search Bar di fragment_home.xml
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                homeViewModel.searchPosts(s.toString()) // Panggil fungsi search di ViewModel
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Listener untuk FAB Add Post di fragment_home.xml
        binding.fabAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addPostFragment) // Action ke AddPost
        }

        // Listener untuk Tombol Logout di fragment_home.xml
        binding.buttonLogout.setOnClickListener {
            authViewModel.logout() // Panggil logout
        }
    }

    private fun observeViewModel() {
        // Observe daftar post UTAMA yang sudah diperkaya dari ViewModel
        homeViewModel.enrichedPosts.observe(viewLifecycleOwner) { posts ->
            Log.d("HomeFragment", "Enriched posts observed: ${posts?.size ?: 0}")
            // Hanya update jika tidak sedang dalam mode search
            if (binding.editTextSearch.text.isNullOrBlank()) {
                if (::postAdapter.isInitialized) { // Pastikan adapter sudah diinisialisasi
                    postAdapter.submitList(posts)
                }
                binding.textViewNoPosts.visibility = if (posts.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
        }

        // Observe hasil search (sudah diperkaya oleh ViewModel)
        homeViewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            Log.d("HomeFragment", "Search results observed: ${searchResults?.size ?: 0}")
            // Hanya update jika sedang dalam mode search
            if (!binding.editTextSearch.text.isNullOrBlank()) {
                if (::postAdapter.isInitialized) {
                    postAdapter.submitList(searchResults)
                }
                binding.textViewNoPosts.visibility = if (searchResults.isNullOrEmpty()) View.VISIBLE else View.GONE
            } else if (searchResults.isNullOrEmpty() && binding.editTextSearch.text.isNullOrBlank()){
                // Jika search dikosongkan, kembalikan ke daftar utama
                if (::postAdapter.isInitialized) {
                    postAdapter.submitList(homeViewModel.enrichedPosts.value)
                }
                binding.textViewNoPosts.visibility = if (homeViewModel.enrichedPosts.value.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hapus referensi binding untuk mencegah memory leak
        _binding = null
    }
}