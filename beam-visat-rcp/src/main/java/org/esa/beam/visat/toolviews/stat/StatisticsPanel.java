package org.esa.beam.visat.toolviews.stat;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.beam.framework.datamodel.ROIDefinition;
import org.esa.beam.framework.datamodel.Stx;
import org.esa.beam.framework.ui.application.ToolView;
import org.esa.beam.util.StringUtils;

import javax.media.jai.ROI;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A general pane within the statistics window.
 *
 * @author Marco Peters
 */
class StatisticsPanel extends TextPagePanel {

    private static final String DEFAULT_STATISTICS_TEXT = "No statistics computed yet.";  /*I18N*/
    private static final String TITLE_PREFIX = "Statistics";

    private ComputePanel computePanel;
    private ActionListener allPixelsActionListener;
    private ActionListener roiActionListener;

    public StatisticsPanel(final ToolView parentDialog, String helpID) {
        super(parentDialog, DEFAULT_STATISTICS_TEXT, helpID);

    }

    @Override
    protected String getTitlePrefix() {
        return TITLE_PREFIX;
    }

    @Override
    protected void initContent() {
        super.initContent();
        computePanel = ComputePanel.createComputePane(getAllPixelActionListener(), getRoiActionListener(), getRaster());
        final JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(computePanel, BorderLayout.NORTH);
        final JPanel helpPanel = new JPanel(new BorderLayout());
        helpPanel.add(getHelpButton(), BorderLayout.EAST);
        rightPanel.add(helpPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);
    }

    private ActionListener getAllPixelActionListener() {
        if (allPixelsActionListener == null) {
            allPixelsActionListener = new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    computeStatistics(false);
                }
            };
        }
        return allPixelsActionListener;
    }

    private ActionListener getRoiActionListener() {
        if (roiActionListener == null) {
            roiActionListener = new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    computeStatistics(true);
                }
            };
        }
        return roiActionListener;
    }

    @Override
    protected void updateContent() {
        super.updateContent();
        if (computePanel != null) {
            computePanel.setRaster(getRaster());
            getTextArea().setText(DEFAULT_STATISTICS_TEXT);
        }
    }

    @Override
    protected String createText() {
        // not used
        return DEFAULT_STATISTICS_TEXT;
    }

    private void computeStatistics(final boolean useROI) {
        final ROI roi;
        if (useROI) {
            roi = getROI();
        } else {
            roi = null;
        }

        ProgressMonitorSwingWorker<Stx, Object> swingWorker = new ProgressMonitorSwingWorker<Stx, Object>(this, "Computing Statistics") {
            @Override
            protected Stx doInBackground(ProgressMonitor pm) throws Exception {
                final Stx stx;
                if (roi == null) {
                    stx = getRaster().getStx(true, pm);
                } else {
                    stx = Stx.create(getRaster(), roi, pm);
                }
                return stx;
            }


            @Override
            public void done() {

                try {
                    final Stx stx = get();
                    if (stx.getSampleCount() > 0) {
                        getTextArea().setText(createText(stx, roi));
                        getTextArea().setCaretPosition(0);
                    } else {
                        final String msgPrefix;
                        if (useROI) {
                            msgPrefix = "The ROI is empty.";        /*I18N*/
                        } else {
                            msgPrefix = "The scene contains no valid pixels.";  /*I18N*/
                        }
                        JOptionPane.showMessageDialog(getParentDialogContentPane(),
                                                      msgPrefix +
                                                              "\nStatistics have not been computed.", /*I18N*/
                                                                                                      "Statistics", /*I18N*/
                                                                                                      JOptionPane.WARNING_MESSAGE);
                        getTextArea().setText(DEFAULT_STATISTICS_TEXT);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(getParentDialogContentPane(),
                                                  "Failed to compute statistics.\nAn error occured:" + e.getMessage(),
                                                  /*I18N*/
                                                  "Statistics", /*I18N*/
                                                  JOptionPane.ERROR_MESSAGE);
                    getTextArea().setText(DEFAULT_STATISTICS_TEXT);
                }
            }
        };
        swingWorker.execute();
    }

    private String createText(final Stx stat, final ROI roi) {

        final String unit = (StringUtils.isNotNullAndNotEmpty(getRaster().getUnit()) ? getRaster().getUnit() : "1");
        final long numPixelTotal = getRaster().getSceneRasterWidth() * (long) getRaster().getSceneRasterHeight();
        final StringBuffer sb = new StringBuffer(1024);

        sb.append("\n");

        sb.append("Only ROI pixels considered:  \t");
        sb.append(roi != null ? "Yes" : "No");
        sb.append("\n");

        sb.append("Number of pixels total:      \t");
        sb.append(numPixelTotal);
        sb.append("\n");

        sb.append("Number of considered pixels: \t");
        sb.append(stat.getSampleCount());
        sb.append("\n");

        sb.append("Ratio of considered pixels:  \t");
        sb.append(100.0 * stat.getSampleCount() / numPixelTotal);
        sb.append("\t ");
        sb.append("%");
        sb.append("\n");

        sb.append("\n");

        sb.append("Minimum:  \t");
        sb.append(stat.getMin());
        sb.append("\t ");
        sb.append(unit);
        sb.append("\n");

        sb.append("Maximum:  \t");
        sb.append(stat.getMax());
        sb.append("\t ");
        sb.append(unit);
        sb.append("\n");

        sb.append("\n");

        sb.append("Mean:     \t");
        sb.append(stat.getMean());
        sb.append("\t ");
        sb.append(unit);
        sb.append("\n");

        sb.append("Std-Dev:  \t");
        sb.append(stat.getStandardDeviation());
        sb.append("\t ");
        sb.append(unit);
        sb.append("\n");

        if (roi != null) {
            final ROIDefinition roiDefinition = getRaster().getROIDefinition();

            sb.append("\n");

            sb.append("ROI area shapes used: \t");
            sb.append(roiDefinition.isShapeEnabled() ? "Yes" : "No");
            sb.append("\n");

            sb.append("ROI value range used: \t");
            sb.append(roiDefinition.isValueRangeEnabled() ? "Yes" : "No");
            sb.append("\n");

            if (roiDefinition.isValueRangeEnabled()) {
                sb.append("ROI minimum value:   \t");
                sb.append(roiDefinition.getValueRangeMin());
                sb.append("\t ");
                sb.append(unit);
                sb.append("\n");

                sb.append("ROI maximum value:   \t");
                sb.append(roiDefinition.getValueRangeMax());
                sb.append("\t ");
                sb.append(unit);
                sb.append("\n");
            }

            sb.append("ROI bitmask used: \t");
            sb.append(roiDefinition.isBitmaskEnabled() ? "Yes" : "No");
            sb.append("\n");

            if (roiDefinition.isBitmaskEnabled()) {
                sb.append("ROI bitmask expression: \t");
                sb.append(roiDefinition.getBitmaskExpr());
                sb.append("\n");
            }

            sb.append("ROI combination operator: \t");
            sb.append(roiDefinition.isOrCombined() ? "OR" : "AND");
            sb.append("\n");

            sb.append("ROI inverted: \t");
            sb.append(roiDefinition.isInverted() ? "Yes" : "No");
            sb.append("\n");
        }
        return sb.toString();
    }

    protected void handleLayerContentChanged() {
        computePanel.updateRoiCheckBoxState();
    }
}
