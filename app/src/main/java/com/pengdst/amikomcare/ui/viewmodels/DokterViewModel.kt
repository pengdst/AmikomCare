package com.pengdst.amikomcare.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pengdst.amikomcare.datas.models.DokterModel
import com.pengdst.amikomcare.listeners.LoginCallback

class DokterViewModel : ViewModel(){
    private val TAG = "DokterViewModel"
    private val NODE_LOGIN = "login"
    private val NODE_DOKTER = "dokter"

    public var callback: LoginCallback = object : LoginCallback{
        override fun onSuccess(dokter: DokterModel) {

        }

    }

    val liveDataDokter = MutableLiveData<DokterModel>()
    val liveDataDokters = MutableLiveData<MutableList<DokterModel>>()

    private val db = FirebaseDatabase.getInstance().getReference(NODE_LOGIN)
    private val dbDokter = db.child(NODE_DOKTER)

    fun login(email: String, password: String) {
        dbDokter.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var found = false

                for (dokterSnapshots in snapshot.children) {
                    val dokter = dokterSnapshots.getValue(DokterModel::class.java)

                    if ((dokter?.email?.equals(email) == true) && (dokter.password.equals(password))) {
                        dokter.id = dokterSnapshots.key

                        liveDataDokter.value = dokter
                        found = true
                    }
                }

                if (found) {
                    callback.onSuccess(liveDataDokter.value!!)
                    Log.e(TAG, "Found User: ${liveDataDokter.value}")
                }
                else {
                    Log.e(TAG, "Wrong Password or Email: ${!found}")
                }
            }

        })
    }

    fun logout(){
        liveDataDokter.value = null
    }

    fun updateDokter(dokter: DokterModel){

        Log.e(TAG, "updateDokter: ")

        dbDokter.child(dokter.id!!).setValue(dokter)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        liveDataDokter.value = dokter
                    }
                    else{
                        liveDataDokter.value = null
                    }
                }

    }

    fun fetchDokters() {
        dbDokter.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                val dokters = mutableListOf<DokterModel>()

                for (dokterSnapshots in snapshot.children) {

                    val dokters = mutableListOf<DokterModel>()

                    for (dokterSnapshots in snapshot.children) {

                        val dokter = dokterSnapshots.getValue(DokterModel::class.java)

                        dokter?.id = dokterSnapshots.key

                        dokter.let {
                            dokters.add(it!!)
                        }

                        callback.onSuccess(dokter!!)
                    }

                    liveDataDokters.value = dokters
                }

            }

        })
    }

    fun observeDokters(): LiveData<MutableList<DokterModel>> {
        return liveDataDokters
    }
}