package com.ipca.lojasocial.domain.usecase.kit

import com.ipca.lojasocial.domain.model.Kit
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.repository.KitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case para obter todos os kits
 */
class GetAllKitsUseCase @Inject constructor(
    private val repository: KitRepository
) {
    operator fun invoke(): Flow<Result<List<Kit>>> {
        return repository.getAllKits()
    }
}

/**
 * Use case para obter kits ativos
 */
class GetActiveKitsUseCase @Inject constructor(
    private val repository: KitRepository
) {
    operator fun invoke(): Flow<Result<List<Kit>>> {
        return repository.getActiveKits()
    }
}

/**
 * Use case para obter kit por ID
 */
class GetKitByIdUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kitId: String): Result<Kit> {
        if (kitId.isBlank()) {
            return Result.Error(Exception("ID do kit inválido"))
        }
        return repository.getKitById(kitId)
    }
}

/**
 * Use case para criar novo kit
 */
class CreateKitUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kit: Kit): Result<Kit> {
        // Validações
        if (kit.name.isBlank()) {
            return Result.Error(Exception("Nome do kit é obrigatório"))
        }

        if (kit.items.isEmpty()) {
            return Result.Error(
                Exception("Kit deve conter pelo menos um produto")
            )
        }

        // Validar cada item
        for (item in kit.items) {
            if (item.productId.isBlank()) {
                return Result.Error(
                    Exception("Produto inválido no kit")
                )
            }

            if (item.quantity <= 0) {
                return Result.Error(
                    Exception("Quantidade deve ser maior que zero")
                )
            }
        }

        return repository.createKit(kit)
    }
}

/**
 * Use case para atualizar kit
 */
class UpdateKitUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kit: Kit): Result<Kit> {
        // Validações
        if (kit.id.isBlank()) {
            return Result.Error(Exception("ID do kit é obrigatório"))
        }

        if (kit.name.isBlank()) {
            return Result.Error(Exception("Nome do kit é obrigatório"))
        }

        if (kit.items.isEmpty()) {
            return Result.Error(
                Exception("Kit deve conter pelo menos um produto")
            )
        }

        // Validar cada item
        for (item in kit.items) {
            if (item.productId.isBlank()) {
                return Result.Error(
                    Exception("Produto inválido no kit")
                )
            }

            if (item.quantity <= 0) {
                return Result.Error(
                    Exception("Quantidade deve ser maior que zero")
                )
            }
        }

        return repository.updateKit(kit)
    }
}

/**
 * Use case para deletar kit (soft delete)
 */
class DeleteKitUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kitId: String): Result<Unit> {
        if (kitId.isBlank()) {
            return Result.Error(Exception("ID do kit inválido"))
        }

        return repository.deleteKit(kitId)
    }
}

/**
 * Use case para pesquisar kits
 */
class SearchKitsUseCase @Inject constructor(
    private val repository: KitRepository
) {
    operator fun invoke(query: String): Flow<Result<List<Kit>>> {
        if (query.isBlank()) {
            return repository.getAllKits()
        }
        return repository.searchKits(query)
    }
}
