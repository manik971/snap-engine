package org.esa.snap.binning.operator.metadata;

import org.esa.snap.framework.datamodel.MetadataElement;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.util.StringUtils;

class AllHistoriesMetaAggregator extends AbstractMetadataAggregator {

    @Override
    public void aggregateMetadata(Product product) {
        final String productName = Utilities.extractProductName(product);
        final MetadataElement processingGraphElement = Utilities.getProcessingGraphElement(product);

        aggregate(productName, processingGraphElement);
    }

    @Override
    public void aggregateMetadata(MetadataElement processingGraphElement) {
        String productName = Utilities.extractProductName(processingGraphElement);
        if (StringUtils.isNullOrEmpty(productName)) {
            productName = "unknown";
        }

        aggregate(productName, processingGraphElement);
    }

    @Override
    public MetadataElement getMetadata() {
        return inputsMetaElement;
    }

    private void aggregate(String productName, MetadataElement processingGraphElement) {
        final MetadataElement productElement = Utilities.createSourceMetaElement(productName, aggregatedCount);
        if (processingGraphElement != null) {
            productElement.addElement(processingGraphElement.createDeepClone());
        }
        inputsMetaElement.addElementAt(productElement, aggregatedCount);
        ++aggregatedCount;
    }
}
