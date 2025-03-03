package com.akashdas.recipesearchapp.Fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.akashdas.recipesearchapp.R
import com.akashdas.recipesearchapp.ViewModel.ProfileViewModel
import com.akashdas.recipesearchapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var auth: FirebaseAuth  // Explicitly declare auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()  // Initialize auth here
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe ViewModel data
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.emails.text = "Email: ${userData.email}"
            binding.password.text = "Password: ${userData.password}"
        }

        // Fetch data via ViewModel
        viewModel.readDataFromFirebase()

        // Navigation
        binding.back.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }

        binding.login.setOnClickListener {
            auth.signOut()  // Use the instance directly
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    // Optional: Keep this if you want manual control outside ViewModel
    private fun readDataFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val myRef = database.getReference("users").child(user.uid)
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val email = dataSnapshot.child("email").getValue(String::class.java)
                        val password = dataSnapshot.child("password").getValue(String::class.java)
                        binding.emails.text = "Email: $email"
                        binding.password.text = "Password: $password"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("ProfileFragment", "Failed to read value.", error.toException())
                }
            })
        } else {
            Log.w("ProfileFragment", "No authenticated user found.")
        }
    }
}