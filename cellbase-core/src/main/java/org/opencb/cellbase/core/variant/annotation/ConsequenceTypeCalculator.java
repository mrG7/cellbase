package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.MiRNAGene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.regulatory.RegulatoryRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fjlopez on 19/06/15.
 */
public abstract class ConsequenceTypeCalculator {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected HashSet<String> SoNames = new HashSet<>();
    protected ConsequenceType consequenceType;
    protected Gene gene;
    protected Transcript transcript;
    protected GenomicVariant variant;

    public List<ConsequenceType> run(GenomicVariant variant, List<Gene> geneList,
                                     List<RegulatoryRegion> regulatoryRegionList) { return null; }

    protected Boolean regionsOverlap(Integer region1Start, Integer region1End, Integer region2Start, Integer region2End) {
        return (region2Start <= region1End && region2End >= region1Start);
    }

    protected void solveRegulatoryRegions(List<RegulatoryRegion> regulatoryRegionList, List<ConsequenceType> consequenceTypeList) {
        if(!regulatoryRegionList.isEmpty()) {
            consequenceTypeList.add(new ConsequenceType(VariantAnnotationUtils.REGULATORY_REGION_VARIANT));
            boolean TFBSFound=false;
            for(int i=0; (i<regulatoryRegionList.size() && !TFBSFound); i++) {
                String regulatoryRegionType = regulatoryRegionList.get(i).getType();
                TFBSFound = regulatoryRegionType!=null && (regulatoryRegionType.equals("TF_binding_site") ||
                        regulatoryRegionList.get(i).getType().equals("TF_binding_site_motif"));
            }
            if(TFBSFound) {
                consequenceTypeList.add(new ConsequenceType(VariantAnnotationUtils.TF_BINDING_SITE_VARIANT));
            }
        }
    }

    protected void decideStopCodonModificationAnnotation(Set<String> SoNames, String referenceCodon,
                                                         char[] modifiedCodonArray) {
        if (VariantAnnotationUtils.isSynonymousCodon.get(referenceCodon).get(String.valueOf(modifiedCodonArray))) {
            if (VariantAnnotationUtils.isStopCodon(referenceCodon)) {
                SoNames.add(VariantAnnotationUtils.STOP_RETAINED_VARIANT);
            }
        } else {
            if (VariantAnnotationUtils.isStopCodon(String.valueOf(referenceCodon))) {
                SoNames.add(VariantAnnotationUtils.STOP_LOST);
            } else if (VariantAnnotationUtils.isStopCodon(String.valueOf(modifiedCodonArray))) {
                SoNames.add(VariantAnnotationUtils.STOP_GAINED);
            }
        }
    }

    protected void solveMiRNA(int cdnaVariantStart, int cdnaVariantEnd, boolean isIntronicVariant) {
        if (transcript.getBiotype().equals(VariantAnnotationUtils.MIRNA)) {  // miRNA with miRBase data
            if(gene.getMirna()!=null) {
                if (cdnaVariantStart == -1) {  // Probably deletion starting before the miRNA location
                    cdnaVariantStart = 1;       // Truncate to the first transcript position to avoid null exception
                }
                if (cdnaVariantEnd == -1) {    // Probably deletion ending after the miRNA location
                    cdnaVariantEnd = gene.getMirna().getSequence().length();  // Truncate to the last transcript position to avoid null exception
                }
                List<MiRNAGene.MiRNAMature> miRNAMatureList = gene.getMirna().getMatures();
                int i = 0;
                while (i < miRNAMatureList.size() && !regionsOverlap(miRNAMatureList.get(i).cdnaStart,
                        miRNAMatureList.get(i).cdnaEnd, cdnaVariantStart, cdnaVariantEnd)) {
                    i++;
                }
                if (i < miRNAMatureList.size()) {  // Variant overlaps at least one mature miRNA
                    SoNames.add(VariantAnnotationUtils.MATURE_MIRNA_VARIANT);
                } else {
                    if (!isIntronicVariant) {  // Exon variant
                        SoNames.add(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_EXON_VARIANT);
                    }
                    SoNames.add(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_VARIANT);
                }
            } else {
                addNonCodingSOs(isIntronicVariant);
            }
        } else {
            addNonCodingSOs(isIntronicVariant);
        }
    }

    protected void addNonCodingSOs(boolean isIntronicVariant) {
        if (!isIntronicVariant) {  // Exon variant
            SoNames.add(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_EXON_VARIANT);
        }
        SoNames.add(VariantAnnotationUtils.NON_CODING_TRANSCRIPT_VARIANT);
    }


}
