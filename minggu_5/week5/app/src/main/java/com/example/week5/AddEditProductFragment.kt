package com.example.week5

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
// import android.widget.Button // Hapus import Button jika tidak digunakan lagi
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton // <<<--- IMPORT MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddEditProductFragment : Fragment() {

    private lateinit var viewModel: AddEditProductViewModel
    private val retailerSharedViewModel: RetailerSharedViewModel by activityViewModels() // To get retailer info
    private val args: AddEditProductFragmentArgs by navArgs() // Get productId argument

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var etProductName: TextInputEditText
    private lateinit var etProductPrice: TextInputEditText
    private lateinit var etProductDescription: TextInputEditText
    private lateinit var etProductStock: TextInputEditText
    private lateinit var btnSave: MaterialButton // <<<--- UBAH TIPE DEKLARASI
    private lateinit var progressBar: ProgressBar
    private lateinit var tilProductName: TextInputLayout
    private lateinit var tilProductPrice: TextInputLayout
    private lateinit var tilProductDescription: TextInputLayout
    private lateinit var tilProductStock: TextInputLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[AddEditProductViewModel::class.java]
        return inflater.inflate(R.layout.fragment_add_edit_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupToolbar()
        setupClickListeners()
        setupObservers()

        // Load existing product data if editing, otherwise prepare for adding
        viewModel.loadProductForEdit(args.productId)
        // Also load retailer info (needed for saving)
        retailerSharedViewModel.loadCurrentUser()

    }

    private fun setupViews(view: View) {
        toolbar = view.findViewById(R.id.toolbarAddEdit)
        etProductName = view.findViewById(R.id.etProductName)
        etProductPrice = view.findViewById(R.id.etProductPrice)
        etProductDescription = view.findViewById(R.id.etProductDescription)
        etProductStock = view.findViewById(R.id.etProductStock)
        btnSave = view.findViewById(R.id.btnAddEditProduct) // findViewById akan mengembalikan MaterialButton
        progressBar = view.findViewById(R.id.progressBarAddEdit)
        tilProductName = view.findViewById(R.id.tilProductName)
        tilProductPrice = view.findViewById(R.id.tilProductPrice)
        tilProductDescription = view.findViewById(R.id.tilProductDescription)
        tilProductStock = view.findViewById(R.id.tilProductStock)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            hideKeyboard()
            findNavController().navigateUp() // Go back
        }
        // Title and Button text set in observers based on mode
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            hideKeyboard()
            val retailer = retailerSharedViewModel.loggedInUser.value
            viewModel.saveProduct(
                retailerEmail = retailer?.email,
                retailerName = retailer?.name,
                productId = args.productId, // Pass ID for editing or null for adding
                name = etProductName.text.toString().trim(),
                priceStr = etProductPrice.text.toString().trim(),
                description = etProductDescription.text.toString().trim(),
                stockStr = etProductStock.text.toString().trim()
            )
        }
    }

    private fun setupObservers() {
        viewModel.isEditMode.observe(viewLifecycleOwner) { isEdit ->
            toolbar.title = if (isEdit) "Edit Product" else "Add Product"
            btnSave.text = if (isEdit) "Save Changes" else "Add Product"
            // Sekarang bisa langsung panggil setIconResource karena tipe btnSave sudah MaterialButton
            btnSave.setIconResource(if(isEdit) R.drawable.ic_edit else R.drawable.ic_add_product) // Set icon
        }

        // When product data loads for editing, populate fields
        viewModel.productToEdit.observe(viewLifecycleOwner) { product ->
            // Pastikan hanya mengisi jika sedang dalam mode edit dan produk tidak null
            if (viewModel.isEditMode.value == true && product != null) {
                etProductName.setText(product.name)
                // Format harga menjadi string biasa (tanpa simbol mata uang) untuk edit
                etProductPrice.setText(product.price.toBigDecimal().toPlainString())
                etProductDescription.setText(product.description)
                etProductStock.setText(product.stock.toString())
            }
        }


        viewModel.isLoadingData.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Disable fields while loading/saving
            etProductName.isEnabled = !isLoading
            etProductPrice.isEnabled = !isLoading
            etProductDescription.isEnabled = !isLoading
            etProductStock.isEnabled = !isLoading
            btnSave.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                showToast(it) // Show validation errors as Toast
                viewModel.resetOperationStatus() // Clear error after showing
            }
        }

        viewModel.operationStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is OperationResult.Success -> {
                    showToast(result.message)
                    findNavController().navigateUp() // Go back after successful save
                    // Reset status tidak perlu di sini karena sudah navigateUp
                }
                is OperationResult.Error -> {
                    // Error message handled by _error observer, reset sudah dipanggil di sana
                }
                OperationResult.Idle -> { /* Do nothing */ }
            }
        }
    }

    private fun showToast(message: String) {
        // Pastikan context tidak null sebelum membuat Toast
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        // Pastikan view tidak null sebelum mencoba menyembunyikan keyboard
        view?.let { currentView ->
            imm?.hideSoftInputFromWindow(currentView.windowToken, 0)
        }
    }
}