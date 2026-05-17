package com.example.statussaver.ui.adapters

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.statussaver.R
import com.example.statussaver.databinding.DialogGuideBinding
import com.example.statussaver.databinding.DialogRateUsBinding
import com.example.statussaver.databinding.ItemSettingsBinding
import com.example.statussaver.models.SettingsModel

class SettingsAdapter(
    private var list: ArrayList<SettingsModel>,
    private var context: Context
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    inner class ViewHolder(
        var binding: ItemSettingsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: SettingsModel, position: Int) {

            binding.apply {

                settingsTitle.text = model.title
                settingsDesc.text = model.desc

                root.setOnClickListener {

                    when (position) {

                        0 -> {
                            showGuideDialog()
                        }

                        2 -> {
                            openUrl(
                                "https://sites.google.com/view/status-saver-disclaimer-00/home"
                            )
                        }

                        3 -> {
                            openUrl(
                                "https://sites.google.com/view/privacy-policy-status-saver-00/home"
                            )
                        }

                        4 -> {
                            shareApp()
                        }

                        5 -> {
                            showRateUsDialog()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemSettingsBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    private fun showGuideDialog() {

        val dialog = Dialog(context)

        val binding = DialogGuideBinding.inflate(
            (context as Activity).layoutInflater
        )

        dialog.setContentView(binding.root)

        dialog.window?.setLayout(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT
        )

        binding.okayBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRateUsDialog() {

        val dialog = Dialog(context)

        val binding = DialogRateUsBinding.inflate(
            LayoutInflater.from(context)
        )

        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        dialog.window?.setLayout(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT
        )

        dialog.setCancelable(true)

        val stars = listOf(
            binding.imageButton2,
            binding.imageButton,
            binding.imageButton3,
            binding.imageButton4,
            binding.imageButton5
        )

        var selectedStars = 0

        fun updateRating(rating: Int) {

            selectedStars = rating

            stars.forEachIndexed { index, imageButton ->

                if (index < rating) {
                    imageButton.setImageResource(R.drawable.ic_selectedstar)
                } else {
                    imageButton.setImageResource(R.drawable.ic_defaultstar)
                }
            }

            when (rating) {

                1 -> binding.emogis.setImageResource(R.drawable.ic_emogi1)

                2 -> binding.emogis.setImageResource(R.drawable.ic_emogi2)

                3 -> binding.emogis.setImageResource(R.drawable.ic_emogi3)

                4 -> binding.emogis.setImageResource(R.drawable.ic_emogi4)

                5 -> binding.emogis.setImageResource(R.drawable.ic_emogi1)
            }
        }

        binding.imageButton2.setOnClickListener {
            updateRating(1)
        }

        binding.imageButton.setOnClickListener {
            updateRating(2)
        }

        binding.imageButton3.setOnClickListener {
            updateRating(3)
        }

        binding.imageButton4.setOnClickListener {
            updateRating(4)
        }

        binding.imageButton5.setOnClickListener {
            updateRating(5)
        }

        binding.materialbutton.setOnClickListener {

            if (selectedStars == 0) {

                Toast.makeText(
                    context,
                    "Please select rating",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            try {

                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "market://details?id=${context.packageName}"
                    )
                )

                context.startActivity(intent)

            } catch (e: Exception) {

                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://play.google.com/store/apps/details?id=${context.packageName}"
                    )
                )

                context.startActivity(intent)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openUrl(url: String) {

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )

        context.startActivity(intent)
    }

    private fun shareApp() {

        val intent = Intent(Intent.ACTION_SEND).apply {

            type = "text/plain"

            putExtra(
                Intent.EXTRA_SUBJECT,
                context.getString(R.string.app_name)
            )

            putExtra(
                Intent.EXTRA_TEXT,
                "My App is so cool please download it:\nhttps://play.google.com/store/apps/details?id=${context.packageName}"
            )
        }

        context.startActivity(
            Intent.createChooser(intent, "Share via")
        )
    }
}