package tv.teads.teadssdkdemo.v6.ui.base

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.teads.teadssdkdemo.v6.ui.base.components.ChipGroup
import tv.teads.teadssdkdemo.v6.ui.base.components.DemoTextField
import tv.teads.teadssdkdemo.v6.ui.base.components.FormatDescription
import tv.teads.teadssdkdemo.v6.ui.base.components.Section
import tv.teads.teadssdkdemo.v6.ui.base.components.TeadsButton

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
    viewModel: DemoViewModel = viewModel()
) {
    val placementId by viewModel.placementId.collectAsState()
    val articleUrl by viewModel.articleUrl.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(viewModel.scrollState)
    ) {
        // Format Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Section(title = "Format", modifier = Modifier.padding(top = 12.dp)) {
                ChipGroup(
                    chips = viewModel.getFormatChips(),
                    onChipClick = {},
                )

                FormatDescription(
                    selectedFormat = viewModel.selectedFormat
                )
            }
        }

        // Placement ID Configuration Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Section(title = "Placement Configurations") {
                // Article URL Text Field
                DemoTextField(
                    value = articleUrl,
                    onValueChange = viewModel::onArticleUrlChange,
                    label = "Article URL",
                    modifier = Modifier.fillMaxWidth()
                )

                // Placement ID Text Field
                DemoTextField(
                    value = placementId,
                    onValueChange = viewModel::updatePlacementId,
                    label = "Placement ID",
                    keyboardType = viewModel.getInputMethod(),
                    modifier = Modifier.fillMaxWidth()
                )

                // PID Chips
                ChipGroup(
                    chips = viewModel.getPidChips(),
                    onChipClick = viewModel::onPidChipClick
                )
            }
        }

        // Integrations Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Section(title = "Integrations") {
                ChipGroup(
                    chips = viewModel.getIntegrationChips(),
                    onChipClick = viewModel::onIntegrationChipClick
                )
            }
        }

        // LAUNCH Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TeadsButton(
                text = "LAUNCH ARTICLE",
                onClick = { viewModel.launchNavigation() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
