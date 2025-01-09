package com.dicoding.jetreward.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.jetreward.data.RewardRepository
import com.dicoding.jetreward.model.OrderReward
import com.dicoding.jetreward.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: RewardRepository
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState<List<OrderReward>>> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState<List<OrderReward>>>
        get() = _uiState

    fun getAllRewards() {
        viewModelScope.launch {
            repository.getAllRewards()
                .catch {
                    _uiState.value = UiState.Error(it.message.toString())
                }
                .collect { orderRewards ->
                    _uiState.value = UiState.Success(orderRewards)
                }
        }
    }
}
// Pada kode di atas, kita menggunakan catch untuk menangkap error dan memasukkannya ke dalam UiState.Error.
// Selain itu, juga ada collect untuk mendapatkan data yang berhasil didapatkan dan dimasukkan ke dalam UiState.Success.
// Penggunaan UiState ini sangat bermanfaat untuk menentukan tampilan aplikasi apabila masih dalam proses loading atau terjadi error hanya dengan sebuah class saja.
// Selain itu, dengan mengirimkan event ke ViewModel dan mengembalikan data dalam bentuk UiState, artinya kita menerapkan Unidirectional Data Flow yang efektif.