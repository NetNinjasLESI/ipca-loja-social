package com.ipca.lojasocial.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ipca.lojasocial.domain.model.UserRole
import com.ipca.lojasocial.presentation.ui.screens.auth.login.LoginScreen
import com.ipca.lojasocial.presentation.ui.screens.auth.register.RegisterScreen
import com.ipca.lojasocial.presentation.ui.screens.user.*
import com.ipca.lojasocial.presentation.ui.screens.collaborator.dashboard.CollaboratorDashboardScreen
import com.ipca.lojasocial.presentation.ui.screens.collaborator.applications.*
import com.ipca.lojasocial.presentation.ui.screens.inventory.*
import com.ipca.lojasocial.presentation.ui.screens.beneficiary.*
import com.ipca.lojasocial.presentation.ui.screens.kit.*
import com.ipca.lojasocial.presentation.ui.screens.delivery.*
import com.ipca.lojasocial.presentation.ui.screens.campaign.*
import com.ipca.lojasocial.presentation.ui.screens.guest.PublicCampaignDetailsScreen
import com.ipca.lojasocial.presentation.ui.screens.guest.PublicCampaignsScreen
import com.ipca.lojasocial.presentation.ui.screens.reports.*

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")

    // User (normal user - not beneficiary yet)
    object UserDashboard : Screen("user/dashboard")
    object ApplyForBeneficiary : Screen("user/apply")
    object MyApplication : Screen("user/my-application")

    // Beneficiary (Interface do Beneficiário)
    object BeneficiaryHome : Screen("beneficiary/home")
    object BeneficiaryDeliveries : Screen("beneficiary/deliveries")
    object BeneficiaryDeliveryDetails : Screen("beneficiary/delivery/{deliveryId}") {
        fun createRoute(id: String) = "beneficiary/delivery/$id"
    }

    // Applications (Colaborador gere candidaturas)
    object Applications : Screen("applications")
    object ApplicationDetails : Screen("applications/{applicationId}") {
        fun createRoute(id: String) = "applications/$id"
    }

    // Beneficiaries Management (Colaborador gere beneficiários)
    object Beneficiaries : Screen("beneficiaries")
    object AddBeneficiary : Screen("beneficiaries/add")
    object EditBeneficiary : Screen("beneficiaries/edit/{beneficiaryId}") {
        fun createRoute(id: String) = "beneficiaries/edit/$id"
    }
    object BeneficiaryDetails : Screen("beneficiaries/details/{beneficiaryId}") {
        fun createRoute(id: String) = "beneficiaries/details/$id"
    }

    // Collaborator
    object CollaboratorDashboard : Screen("collaborator/dashboard")

    // Inventory
    object Inventory : Screen("inventory")
    object AddProduct : Screen("inventory/add")
    object EditProduct : Screen("inventory/edit/{productId}") {
        fun createRoute(id: String) = "inventory/edit/$id"
    }
    object ProductDetails : Screen("inventory/details/{productId}") {
        fun createRoute(id: String) = "inventory/details/$id"
    }
    object StockMovement : Screen("inventory/movement/{productId}") {
        fun createRoute(id: String) = "inventory/movement/$id"
    }
    object BarcodeScanner : Screen("barcode-scanner")

    // Kits
    object KitsList : Screen("kits")
    object AddKit : Screen("kits/add")
    object EditKit : Screen("kits/edit/{kitId}") {
        fun createRoute(kitId: String) = "kits/edit/$kitId"
    }
    object KitDetails : Screen("kits/details/{kitId}") {
        fun createRoute(kitId: String) = "kits/details/$kitId"
    }

    // Deliveries
    object DeliveriesList : Screen("deliveries")
    object CreateDelivery : Screen("deliveries/create")
    object DeliveryDetails : Screen("deliveries/details/{deliveryId}") {
        fun createRoute(id: String) = "deliveries/details/$id"
    }

    // Campaigns
    object CampaignsList : Screen("campaigns")
    object CreateCampaign : Screen("campaigns/create")
    object EditCampaign : Screen("campaigns/edit/{campaignId}") {
        fun createRoute(id: String) = "campaigns/edit/$id"
    }
    object CampaignDetails : Screen("campaigns/details/{campaignId}") {
        fun createRoute(id: String) = "campaigns/details/$id"
    }

    // Reports
    object Dashboard : Screen("dashboard")
    object Reports : Screen("reports")

    // Public Campaigns (acesso sem login)
    object PublicCampaigns : Screen("guest/campaigns")
    object PublicCampaignDetails : Screen("guest/campaigns/{campaignId}") {
        fun createRoute(id: String) = "guest/campaigns/$id"
    }

    // ✅ MODIFICADO: Agora aceita beneficiaryId como parâmetro
    object RequestDelivery : Screen("beneficiary/request-delivery/{beneficiaryId}") {
        fun createRoute(beneficiaryId: String) = "beneficiary/request-delivery/$beneficiaryId"
    }

    object DeliveryRequests : Screen("collaborator/delivery-requests")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    currentUserId: String = ""
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // ========== AUTH ==========
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToPublicCampaigns = {
                    navController.navigate(Screen.PublicCampaigns.route)
                },
                onNavigateToUserDashboard = {
                    navController.navigate(Screen.UserDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.BeneficiaryHome.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.CollaboratorDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToUserDashboard = {
                    navController.navigate(Screen.UserDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ========== USER DASHBOARD (Utilizador normal) ==========
        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(
                onNavigateToApply = {
                    navController.navigate(Screen.ApplyForBeneficiary.route)
                },
                onNavigateToMyApplication = {
                    navController.navigate(Screen.MyApplication.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ApplyForBeneficiary.route) {
            ApplyForBeneficiaryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MyApplication.route) {
            MyApplicationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== BENEFICIARY INTERFACE (Para o beneficiário usar) ==========
        composable(Screen.BeneficiaryHome.route) {
            BeneficiaryDashboardScreen(
                onNavigateToDeliveries = {
                    navController.navigate(Screen.BeneficiaryDeliveries.route)
                },
                // ✅ MODIFICADO: Agora passa o beneficiaryId
                onNavigateToRequestDelivery = { beneficiaryId ->
                    navController.navigate(Screen.RequestDelivery.createRoute(beneficiaryId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.BeneficiaryDeliveries.route) {
            BeneficiaryDeliveriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { deliveryId ->
                    navController.navigate(
                        Screen.BeneficiaryDeliveryDetails.createRoute(deliveryId)
                    )
                },
                // ✅ MODIFICADO: Agora passa o currentUserId (caso esta tela tenha botão para solicitar)
                onNavigateToRequestDelivery = {
                    navController.navigate(Screen.RequestDelivery.createRoute(currentUserId))
                }
            )
        }

        composable(
            Screen.BeneficiaryDeliveryDetails.route,
            arguments = listOf(navArgument("deliveryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deliveryId = backStackEntry.arguments?.getString("deliveryId") ?: ""
            BeneficiaryDeliveryDetailsScreen(
                deliveryId = deliveryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== APPLICATIONS (Colaborador gere candidaturas) ==========
        composable(Screen.Applications.route) {
            PendingApplicationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { applicationId ->
                    navController.navigate(Screen.ApplicationDetails.createRoute(applicationId))
                }
            )
        }

        composable(
            Screen.ApplicationDetails.route,
            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
            ApplicationDetailsScreen(
                applicationId = applicationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== COLLABORATOR DASHBOARD ==========
        composable(Screen.CollaboratorDashboard.route) {
            CollaboratorDashboardScreen(
                onNavigate = { navController.navigate(it) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ========== INVENTORY ==========
        composable(Screen.Inventory.route) {
            InventoryScreen(
                onNavigateToAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onNavigateToProductDetails = {
                    navController.navigate(Screen.ProductDetails.createRoute(it))
                },
                onNavigateToStockMovement = {
                    navController.navigate(Screen.StockMovement.createRoute(it))
                }
            )
        }

        // ✅ ATUALIZADO: AddProduct com scanner
        composable(Screen.AddProduct.route) {
            // Receber código escaneado
            val scannedBarcode = navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<String>("scanned_barcode")
                ?.value

            AddEditProductScreen(
                productId = null,
                userId = currentUserId,
                scannedBarcode = scannedBarcode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanner = {
                    navController.navigate(Screen.BarcodeScanner.route)
                }
            )

            // Limpar código após uso
            if (scannedBarcode != null) {
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>("scanned_barcode")
            }
        }

        // ✅ ATUALIZADO: EditProduct com scanner
        composable(
            Screen.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")

            // Receber código escaneado
            val scannedBarcode = navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<String>("scanned_barcode")
                ?.value

            AddEditProductScreen(
                productId = productId,
                userId = currentUserId,
                scannedBarcode = scannedBarcode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanner = {
                    navController.navigate(Screen.BarcodeScanner.route)
                }
            )

            // Limpar código após uso
            if (scannedBarcode != null) {
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>("scanned_barcode")
            }
        }

        composable(
            Screen.ProductDetails.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            ProductDetailsScreen(
                productId = it.arguments?.getString("productId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditProduct.createRoute(id))
                },
                onNavigateToStockMovement = { id ->
                    navController.navigate(Screen.StockMovement.createRoute(id))
                }
            )
        }

        composable(
            Screen.StockMovement.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            StockMovementScreen(
                it.arguments?.getString("productId") ?: "",
                currentUserId
            ) { navController.popBackStack() }
        }

        // ✅ NOVO: Barcode Scanner
        composable(Screen.BarcodeScanner.route) {
            BarcodeScannerScreen(
                onBarcodeScanned = { barcode ->
                    // Guardar código e voltar
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_barcode", barcode)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== BENEFICIARIES MANAGEMENT (Colaborador gere) ==========
        composable(Screen.Beneficiaries.route) {
            BeneficiariesListScreen(
                onNavigateToAddBeneficiary = {
                    navController.navigate(Screen.AddBeneficiary.route)
                },
                onNavigateToBeneficiaryDetails = {
                    navController.navigate(Screen.BeneficiaryDetails.createRoute(it))
                }
            )
        }

        composable(Screen.AddBeneficiary.route) {
            AddEditBeneficiaryScreen(
                beneficiaryId = null,
                userId = currentUserId
            ) { navController.popBackStack() }
        }

        composable(
            Screen.EditBeneficiary.route,
            arguments = listOf(navArgument("beneficiaryId") { type = NavType.StringType })
        ) {
            AddEditBeneficiaryScreen(
                it.arguments?.getString("beneficiaryId"),
                currentUserId
            ) { navController.popBackStack() }
        }

        composable(
            Screen.BeneficiaryDetails.route,
            arguments = listOf(navArgument("beneficiaryId") { type = NavType.StringType })
        ) {
            BeneficiaryDetailsScreen(
                beneficiaryId = it.arguments?.getString("beneficiaryId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditBeneficiary.createRoute(id))
                }
            )
        }

        // ========== KITS ==========
        composable(Screen.KitsList.route) {
            KitsListScreen(
                onNavigateToAddKit = {
                    navController.navigate(Screen.AddKit.route)
                },
                onNavigateToKitDetails = {
                    navController.navigate(Screen.KitDetails.createRoute(it))
                }
            )
        }

        composable(Screen.AddKit.route) {
            AddEditKitScreen(
                kitId = null,
                userId = currentUserId
            ) { navController.popBackStack() }
        }

        composable(
            Screen.EditKit.route,
            arguments = listOf(navArgument("kitId") { type = NavType.StringType })
        ) {
            AddEditKitScreen(
                it.arguments?.getString("kitId") ?: "",
                currentUserId
            ) { navController.popBackStack() }
        }

        composable(
            Screen.KitDetails.route,
            arguments = listOf(navArgument("kitId") { type = NavType.StringType })
        ) {
            KitDetailsScreen(
                kitId = it.arguments?.getString("kitId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditKit.createRoute(id))
                }
            )
        }

        // ========== DELIVERIES ==========
        composable(Screen.DeliveriesList.route) {
            DeliveriesListScreen(
                onNavigateToCreateDelivery = {
                    navController.navigate(Screen.CreateDelivery.route)
                },
                onNavigateToDeliveryDetails = {
                    navController.navigate(Screen.DeliveryDetails.createRoute(it))
                }
            )
        }

        composable(Screen.CreateDelivery.route) {
            CreateDeliveryScreen(
                userId = currentUserId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.DeliveryDetails.route,
            arguments = listOf(navArgument("deliveryId") { type = NavType.StringType })
        ) {
            DeliveryDetailsScreen(
                deliveryId = it.arguments?.getString("deliveryId") ?: "",
                userId = currentUserId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== CAMPAIGNS ==========
        composable(Screen.CampaignsList.route) {
            CampaignsListScreen(
                onNavigateToCreateCampaign = {
                    navController.navigate(Screen.CreateCampaign.route)
                },
                onNavigateToCampaignDetails = {
                    navController.navigate(Screen.CampaignDetails.createRoute(it))
                }
            )
        }

        composable(Screen.CreateCampaign.route) {
            CreateEditCampaignScreen(
                campaignId = null,
                userId = currentUserId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.EditCampaign.route,
            arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
        ) {
            CreateEditCampaignScreen(
                campaignId = it.arguments?.getString("campaignId"),
                userId = currentUserId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.CampaignDetails.route,
            arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
        ) {
            CampaignDetailsScreen(
                campaignId = it.arguments?.getString("campaignId") ?: "",
                userId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditCampaign.createRoute(id))
                }
            )
        }

        // ========== REPORTS & DASHBOARD ==========
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route)
                }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== PUBLIC CAMPAIGNS (Sem login) - PACKAGE: guest ==========
        composable(Screen.PublicCampaigns.route) {
            PublicCampaignsScreen(
                onNavigateToCampaignDetails = { campaignId ->
                    navController.navigate(Screen.PublicCampaignDetails.createRoute(campaignId))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(
            Screen.PublicCampaignDetails.route,
            arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
        ) { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId") ?: ""
            PublicCampaignDetailsScreen(
                campaignId = campaignId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // ========================================
        // ✅ NOVO: Construtor Livre de Kits
        // ========================================
        composable(
            Screen.RequestDelivery.route,
            arguments = listOf(navArgument("beneficiaryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val beneficiaryId = backStackEntry.arguments?.getString("beneficiaryId") ?: ""

            CustomKitBuilderScreen(
                beneficiaryId = beneficiaryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DeliveryRequests.route) {
            DeliveryRequestsManagementScreen(
                userId = currentUserId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

fun getStartDestination(userRole: UserRole?): String =
    when (userRole) {
        UserRole.USER -> Screen.UserDashboard.route
        UserRole.BENEFICIARY -> Screen.BeneficiaryHome.route
        UserRole.COLLABORATOR, UserRole.ADMINISTRATOR -> Screen.CollaboratorDashboard.route
        null -> Screen.Login.route
    }