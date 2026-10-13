package com.ramirezpenayo.placeshare.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ramirezpenayo.placeshare.core.domain.model.DomainError
import com.ramirezpenayo.placeshare.core.ui.R

@Composable
fun DomainError.asMessage(): String = when (this) {
    DomainError.NetworkError -> stringResource(R.string.core_ui_error_network)
    DomainError.NotFound -> stringResource(R.string.core_ui_not_found)
    DomainError.EmptyLocation -> stringResource(R.string.core_ui_empty_location)
    is DomainError.UnknownError -> message ?: stringResource(R.string.core_ui_unknown)
}
