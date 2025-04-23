package com.example.myapplication // <-- Sesuaikan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentAccountUserBinding
import java.text.NumberFormat
import java.util.Locale

class AccountUserFragment : Fragment() {

    private var _binding: FragmentAccountUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayUserInfo()
        setupButtonClickListeners()
    }

    // Dipanggil lagi di onResume agar balance terupdate setelah topup
    override fun onResume() {
        super.onResume()
        displayUserInfo()
    }

    private fun displayUserInfo() {
        // Tampilkan nama profile
        binding.textViewProfileName.text = UserCredentialsManager.getCurrentUserProfileName(requireContext())

        // Tampilkan balance wallet
        val balance = UserCredentialsManager.getWalletBalance(requireContext())
        binding.textViewWalletBalanceAccount.text = formatCurrency(balance)
    }

    private fun setupButtonClickListeners() {
        binding.buttonLibrary.setOnClickListener {
            findNavController().navigate(R.id.action_accountUserFragment_to_libraryUserFragment)
        }

        binding.buttonTopup.setOnClickListener {
            findNavController().navigate(R.id.action_accountUserFragment_to_topupUserFragment)
        }

        binding.buttonLogout.setOnClickListener {
            UserCredentialsManager.logoutUser() // Penting: Hapus state login
            findNavController().navigate(R.id.action_global_logout) // Gunakan action global
        }
    }

    private fun formatCurrency(value: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}