package com.example.recycleapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.recycleapp.databinding.ActivityInstructionsBinding

class InstructionsActivity: AppCompatActivity() {

    private lateinit var binding: ActivityInstructionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityInstructionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val productName=intent.getStringExtra("productName")?:"Unknown product"
        val instructions=intent.getStringExtra("instructions")?:"No instructions available"
        binding.detailsTxtView.text=instructions
        binding.productNameBox.text=productName

        val lowerCaseInstructions=instructions.lowercase()
        when{
            lowerCaseInstructions.contains("plastic")|| lowerCaseInstructions.contains("bottle")->{
                binding.ProductImageView.setImageResource(R.drawable.galben_bin_plastic)
            }
            lowerCaseInstructions.contains("cardboard") || lowerCaseInstructions.contains("paper") || lowerCaseInstructions.contains("box") -> {
                binding.ProductImageView.setImageResource(R.drawable.albastru_bin_hartie)
            }
            lowerCaseInstructions.contains("glass") -> {
                binding.ProductImageView.setImageResource(R.drawable.verde_bin_sticla)
            }
            lowerCaseInstructions.contains("metal") || lowerCaseInstructions.contains("aluminum") || lowerCaseInstructions.contains("can") -> {
                binding.ProductImageView.setImageResource(R.drawable.galben_bin_plastic)
            }
            else -> {
                binding.ProductImageView.setImageResource(R.drawable.negru_bin_rezidual)
            }
        }
    }
}