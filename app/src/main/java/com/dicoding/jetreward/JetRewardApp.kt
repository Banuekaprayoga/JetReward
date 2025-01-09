package com.dicoding.jetreward

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dicoding.jetreward.ui.navigation.NavigationItem
import com.dicoding.jetreward.ui.navigation.Screen
import com.dicoding.jetreward.ui.screen.cart.CartScreen
import com.dicoding.jetreward.ui.screen.detail.DetailScreen
import com.dicoding.jetreward.ui.screen.home.HomeScreen
import com.dicoding.jetreward.ui.screen.profile.ProfileScreen
import com.dicoding.jetreward.ui.theme.JetRewardTheme

@Composable
fun JetRewardApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.DetailReward.route) {
                BottomBar(navController)
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    navigateToDetail = { rewardId ->
                        navController.navigate(Screen.DetailReward.createRoute(rewardId))
                    }
                )
            }
            composable(Screen.Cart.route) {
                val context = LocalContext.current
                CartScreen(
                    onOrderButtonClicked = { message ->
                        shareOrder(context, message)
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(
                route = Screen.DetailReward.route,
                arguments = listOf(navArgument("rewardId") { type = NavType.LongType }),
            ) {
                val id = it.arguments?.getLong("rewardId") ?: -1L
                DetailScreen(
                    rewardId = id,
                    navigateBack = {
                        navController.navigateUp()
                    },
                    navigateToCart = {
                        navController.popBackStack()
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            // Karena mengarah ke halaman cart yang merupakan halaman utama pada Bottom Navigation, kita menggunakan kode yang sama dengan navigasi pada item Bottom Navigation.
        // Bedanya, kita menambahkan popBackStack untuk menghapus stack pada halaman home
            // Tanpa popBackStack, ketika kita menekan menu Home, ia akan menampilkan stack aplikasi terakhir, yakni halaman detail.
        // Dengan popBackStack, stack tersebut dibersihkan sehingga halaman yang tampil adalah halaman awal (home).
        }
    }
}
// Salah satu halaman sudah tampil, yakni Home. Hal ini sesuai dengan value yang diberikan pada parameter startDestination, yakni Screen.Home.route yang mengarah ke halaman Home.

private fun shareOrder(context: Context, summary: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.dicoding_reward))
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    // Intent yang dibuat berjenis ACTION_SEND, artinya hanya aplikasi yang bisa menerima jenis ACTION_SEND sajalah yang bisa menangani tugas ini, seperti aplikasi SMS dan email.
// Kita juga mendefinisikan jenis MIME data yang dibagikan, yakni text/plain. Kemudian untuk memasukkan data, kita menggunakan putExtra.
// EXTRA_SUBJECT digunakan untuk judul dan EXTRA_TEXT digunakan untuk isi pesan.

    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.dicoding_reward)
        )
        // createChooser merupakan jenis eksekusi Intent yang menampilkan beberapa pilihan aplikasi yang bisa membuka data yang bagikan.
    )
}

// Catatan:
//Sebagai best-practice, NavController sebaiknya diletakkan sebagai parameter untuk memudahkan testing.
@Composable
private fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
    ) {
        // kita membandingkan route dari back stack dengan route pada navigation item. Jika sama (alias dipilih), parameter selected akan bernilai true.
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val navigationItem = listOf(
            NavigationItem(
                title = stringResource(R.string.menu_home),
                icon = Icons.Default.Home,
                screen = Screen.Home
            ),
            NavigationItem(
                title = stringResource(R.string.menu_cart),
                icon = Icons.Default.ShoppingCart,
                screen = Screen.Cart
            ),
            NavigationItem(
                title = stringResource(R.string.menu_profile),
                icon = Icons.Default.AccountCircle,
                screen = Screen.Profile
            ),
        )
        navigationItem.map { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState =  true
                        launchSingleTop =  true
                    }
                }
                // Berikut beberapa hal yang perlu Anda ketahui dari kode di atas.
                //navController.navigate: digunakan untuk eksekusi navigasi ke route sesuai dengan item yang dipilih.
                //popUpTo: digunakan untuk kembali ke halaman awal supaya tidak membuka halaman baru terus menerus.
                //saveState dan restoreState: mengembalikan state ketika item dipilih lagi.
                //launchSingleTop: digunakan supaya tidak ada halaman yang dobel ketika memilih ulang item yang sama.
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JetHeroesAppPreview() {
    JetRewardTheme {
        JetRewardApp()
    }
}

// Mari kita rekap yang sudah Anda lakukan pada latihan ini.
//
//Membuat NavHost, NavGraph, dan NavController.
//Mengintegrasikan Navigation Component dengan Bottom Navigation.
//Mengirimkan data argument antara halaman.
//Menerapkan prinsip-prinsip navigasi yang tepat.
//Memanfaatkan Intent untuk navigasi ke aplikasi lain.
