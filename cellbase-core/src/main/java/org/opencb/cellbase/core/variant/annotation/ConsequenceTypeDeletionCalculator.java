package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.MiRNAGene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.biodata.models.variation.ProteinVariantAnnotation;
import org.opencb.cellbase.core.common.GenomeSequenceFeature;
import org.opencb.cellbase.core.common.regulatory.RegulatoryRegion;
import org.opencb.cellbase.core.db.api.core.GenomeDBAdaptor;
import org.opencb.datastore.core.QueryOptions;

import java.util.*;

/**
 * Created by fjlopez on 05/08/15.
 */
public class ConsequenceTypeDeletionCalculator extends ConsequenceTypeCalculator {
    private int variantStart;
    private int variantEnd;
    private boolean isBigDeletion;
    private GenomeDBAdaptor genomeDBAdaptor;

    private static final int bigVariantSizeThreshold = 50;


    public ConsequenceTypeDeletionCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        this.genomeDBAdaptor = genomeDBAdaptor;
    }

    public List<ConsequenceType> run(GenomicVariant inputVariant, List<Gene> geneList,
                                     List<RegulatoryRegion> regulatoryRegionList) {

        List<ConsequenceType> consequenceTypeList = new ArrayList<>();
        variant = inputVariant;
        variantEnd = variant.getPosition() + variant.getReference().length() - 1;
        variantStart = variant.getPosition();
        isBigDeletion = ((variantEnd - variantStart) > bigVariantSizeThreshold);
        boolean isIntegernic = true;
        for (Gene currentGene : geneList) {
            gene = currentGene;
            for (Transcript currentTranscript : gene.getTranscripts()) {
                isIntegernic = isIntegernic && (variantEnd < currentTranscript.getStart() ||
                        variantStart > currentTranscript.getEnd());
                transcript = currentTranscript;
                consequenceType = new ConsequenceType();
                consequenceType.setGeneName(gene.getName());
                consequenceType.setEnsemblGeneId(gene.getId());
                consequenceType.setEnsemblTranscriptId(transcript.getId());
                consequenceType.setStrand(transcript.getStrand());
                consequenceType.setBiotype(transcript.getBiotype());
                SoNames.clear();

                if (transcript.getStrand().equals("+")) {
                    if (variantStart <= transcript.getStart() && variantEnd >= transcript.getEnd()) {  // Deletion - whole transcript removed
                        SoNames.add(VariantAnnotationUtils.TRANSCRIPT_ABLATION);
                        consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                        consequenceTypeList.add(consequenceType);
                    } else if (regionsOverlap(transcript.getStart(), transcript.getEnd(), variantStart, variantEnd)) {
                        if (isBigDeletion) {  // Big deletion
                            SoNames.add(VariantAnnotationUtils.FEATURE_TRUNCATION);
                        }
                        switch (transcript.getBiotype()) {
                            /**
                             * Coding biotypes
                             */
                            case VariantAnnotationUtils.NONSENSE_MEDIATED_DECAY:
                                SoNames.add(VariantAnnotationUtils.NMD_TRANSCRIPT_VARIANT);
                            case VariantAnnotationUtils.IG_C_GENE:
                            case VariantAnnotationUtils.IG_D_GENE:
                            case VariantAnnotationUtils.IG_J_GENE:
                            case VariantAnnotationUtils.IG_V_GENE:
                            case VariantAnnotationUtils.TR_C_GENE:  // TR_C_gene
                            case VariantAnnotationUtils.TR_D_GENE:  // TR_D_gene
                            case VariantAnnotationUtils.TR_J_GENE:  // TR_J_gene
                            case VariantAnnotationUtils.TR_V_GENE:  // TR_V_gene
                            case VariantAnnotationUtils.POLYMORPHIC_PSEUDOGENE:
                            case VariantAnnotationUtils.PROTEIN_CODING:    // protein_coding
                            case VariantAnnotationUtils.NON_STOP_DECAY:    // non_stop_decay
                            case VariantAnnotationUtils.TRANSLATED_PROCESSED_PSEUDOGENE:
                            case VariantAnnotationUtils.TRANSLATED_UNPROCESSED_PSEUDOGENE:    // translated_unprocessed_pseudogene
                            case VariantAnnotationUtils.LRG_GENE:    // LRG_gene
                                solveCodingPositiveTranscript();
                                consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                                consequenceTypeList.add(consequenceType);
                                break;
                            /**
                             * Non-coding biotypes
                             */
                            default:
                                solveNonCodingPositiveTranscript();
                                consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                                consequenceTypeList.add(consequenceType);
                                break;
                        }
                    } else {
                        solveTranscriptFlankingRegions(VariantAnnotationUtils.UPSTREAM_GENE_VARIANT,
                                VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT);
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just may have upstream/downstream annotations
                            consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                            consequenceTypeList.add(consequenceType);
                        }
                    }
                } else {
                    if (variantStart <= transcript.getStart() && variantEnd >= transcript.getEnd()) {  // Deletion - whole transcript removed
                        SoNames.add(VariantAnnotationUtils.TRANSCRIPT_ABLATION);
                        consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                        consequenceTypeList.add(consequenceType);
                    } else if (regionsOverlap(transcript.getStart(), transcript.getEnd(), variantStart, variantEnd)) {
                        if (isBigDeletion) {  // Big deletion
                            SoNames.add(VariantAnnotationUtils.FEATURE_TRUNCATION);
                        }
                        switch (transcript.getBiotype()) {
                            /**
                             * Coding biotypes
                             */
                            case VariantAnnotationUtils.NONSENSE_MEDIATED_DECAY:
                                SoNames.add(VariantAnnotationUtils.NMD_TRANSCRIPT_VARIANT);
                            case VariantAnnotationUtils.IG_C_GENE:
                            case VariantAnnotationUtils.IG_D_GENE:
                            case VariantAnnotationUtils.IG_J_GENE:
                            case VariantAnnotationUtils.IG_V_GENE:
                            case VariantAnnotationUtils.TR_C_GENE:  // TR_C_gene
                            case VariantAnnotationUtils.TR_D_GENE:  // TR_D_gene
                            case VariantAnnotationUtils.TR_J_GENE:  // TR_J_gene
                            case VariantAnnotationUtils.TR_V_GENE:  // TR_V_gene
                            case VariantAnnotationUtils.POLYMORPHIC_PSEUDOGENE:
                            case VariantAnnotationUtils.PROTEIN_CODING:    // protein_coding
                            case VariantAnnotationUtils.NON_STOP_DECAY:    // non_stop_decay
                            case VariantAnnotationUtils.TRANSLATED_PROCESSED_PSEUDOGENE:
                            case VariantAnnotationUtils.TRANSLATED_UNPROCESSED_PSEUDOGENE:    // translated_unprocessed_pseudogene
                            case VariantAnnotationUtils.LRG_GENE:    // LRG_gene
                                solveCodingNegativeTranscript();
                                consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                                consequenceTypeList.add(consequenceType);
                                break;
                            /**
                             * Non-coding biotypes
                             */
                            default:
                                solveNonCodingNegativeTranscript();
                                consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                                consequenceTypeList.add(consequenceType);
                                break;
                        }
                    } else {
                        solveTranscriptFlankingRegions(VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT,
                                VariantAnnotationUtils.UPSTREAM_GENE_VARIANT);
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just has upstream/downstream annotations
                            consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                            consequenceTypeList.add(consequenceType);
                        }
                    }

                }
            }
        }

//        if (consequenceTypeList.size() == 0 && isIntegernic) {
        if (isIntegernic) {
            consequenceTypeList.add(new ConsequenceType(VariantAnnotationUtils.INTERGENIC_VARIANT));
        }

        solveRegulatoryRegions(regulatoryRegionList, consequenceTypeList);

        return consequenceTypeList;

    }

    private void solveNonCodingNegativeTranscript() {
        Exon exon = transcript.getExons().get(0);
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;

        if (firstCdsPhase == -1 && transcript.getGenomicCodingEnd() >= exon.getStart()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variantEnd <= exon.getEnd()) {
            if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                consequenceType.setcDnaPosition(cdnaVariantStart);
                if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                }
            }
        } else if (variantStart >= exon.getStart()) {
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (variantEnd - exon.getStart());
        } // Variant includes the whole exon. Variant end is located before the exon, variant start is located after the exon

        int exonCounter = 1;
        while (exonCounter < transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getStart() - 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            if (firstCdsPhase == -1 && transcript.getGenomicCodingEnd() >= exon.getStart()) {  // Set firsCdsPhase only when the first coding exon is reached
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(exon.getEnd()+1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if (variantEnd <= exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                    consequenceType.setcDnaPosition(cdnaVariantStart);
                    if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                        cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                    }
                }
            } else if (variantStart >= exon.getStart()) {
                if (variantStart <= exon.getEnd()) {  // Only variant start within the exon  ----||||||||||E||||----
                    cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                } else {  // Variant does not include this exon, variant is located before this exon
                    variantAhead = false;
                }
            } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
            }
            exonCounter++;
        }
        solveMiRNA(cdnaVariantStart, cdnaVariantEnd, junctionSolution[1]);
    }

    private void solveCodingNegativeTranscript() {
        Exon exon = transcript.getExons().get(0);
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;

        if (firstCdsPhase == -1 && transcript.getGenomicCodingEnd() >= exon.getStart()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variantEnd <= exon.getEnd()) {
            if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                consequenceType.setcDnaPosition(cdnaVariantStart);
                if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                }
            }
        } else if (variantStart >= exon.getStart()) {
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (variantEnd - exon.getStart());
        } // Variant includes the whole exon. Variant end is located before the exon, variant start is located after the exon

        int exonCounter = 1;
        while (exonCounter < transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getStart() - 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            transcriptSequence = exon.getSequence() + transcriptSequence;
            if (firstCdsPhase == -1 && transcript.getGenomicCodingEnd() >= exon.getStart()) {  // Set firsCdsPhase only when the first coding exon is reached
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(exon.getEnd()+1, prevSpliceSite, VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_DONOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if (variantEnd <= exon.getEnd()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantEnd >= exon.getStart()) {  // Variant end within the exon
                    cdnaVariantStart = cdnaExonEnd - (variantEnd - exon.getStart());
                    consequenceType.setcDnaPosition(cdnaVariantStart);
                    if (variantStart >= exon.getStart()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                        cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                    }
                }
            } else if (variantStart >= exon.getStart()) {
                if (variantStart <= exon.getEnd()) {  // Only variant start within the exon  ----||||||||||E||||----
                    cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exon.getStart());
                } else {  // Variant does not include this exon, variant is located before this exon
                    variantAhead = false;
                }
            } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
            }
            exonCounter++;
        }
        // Is not intron variant (both ends fall within the same intron)
        if (!junctionSolution[1]) {
            solveExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaVariantStart, cdnaVariantEnd,
                    firstCdsPhase);
        }

    }

    private void solveExonVariantInNegativeTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantStart, int cdnaVariantEnd, int firstCdsPhase) {
        if (variantEnd > transcript.getGenomicCodingEnd()) {
            if (transcript.getEnd() > transcript.getGenomicCodingEnd() || transcript.unconfirmedStart()) {// Check transcript has 3 UTR
                SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
            }
            if (variantStart <= transcript.getGenomicCodingEnd()) {
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
                if (transcript.getCdnaCodingStart() > 0 || !transcript.unconfirmedStart()) {  // cdnaCodingStart<1 if cds_start_NF and phase!=0
                    SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);
                }
                if (variantStart < (transcript.getGenomicCodingStart() + 3)) {
                    SoNames.add(VariantAnnotationUtils.STOP_LOST);
                    if (variantStart < transcript.getGenomicCodingStart()) {
                        if (transcript.getStart() < transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
                            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
                        }
                    }
                }
            }
        } else if (variantEnd >= transcript.getGenomicCodingStart()) {
            int cdnaCodingStart = transcript.getCdnaCodingStart();  // Need to define a local cdnaCodingStart because may modified in two lines below
            if (cdnaVariantStart != -1) {  // cdnaVariantStart may be null if variantEnd falls in an intron
                if (transcript.unconfirmedStart()) {
                    cdnaCodingStart -= ((3 - firstCdsPhase) % 3);
                }
                int cdsVariantStart = cdnaVariantStart - cdnaCodingStart + 1;
                consequenceType.setCdsPosition(cdsVariantStart);
                // First place where protein variant annotation is added to the Consequence type, must create the ProteinVariantAnnotation object
                ProteinVariantAnnotation proteinVariantAnnotation = new ProteinVariantAnnotation();
                proteinVariantAnnotation.setPosition(((cdsVariantStart - 1) / 3) + 1);
                consequenceType.setProteinVariantAnnotation(proteinVariantAnnotation);
            }
            if (variantStart >= transcript.getGenomicCodingStart()) {  // Variant start also within coding region
                solveCodingExonVariantInNegativeTranscript(splicing, transcriptSequence, cdnaCodingStart, cdnaVariantStart,
                        cdnaVariantEnd);
            } else {
                if (transcript.getStart() < transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
                    SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
                }
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
                SoNames.add(VariantAnnotationUtils.STOP_LOST);
            }
        } else if (transcript.getStart() < transcript.getGenomicCodingStart() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
        }
    }

    private void solveCodingExonVariantInNegativeTranscript(boolean splicing, String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantStart, int cdnaVariantEnd) {
        Boolean codingAnnotationAdded = false;

        if(cdnaVariantStart != -1 && cdnaVariantStart<(cdnaCodingStart+3) && (cdnaCodingStart>0 || !transcript.unconfirmedStart())) {  // cdnaVariantStart=null if variant is intronic. cdnaCodingStart<1 if cds_start_NF and phase!=0
            SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);
            codingAnnotationAdded = true;
        }
        if(cdnaVariantEnd!=-1) {
            int finalNtPhase = (transcript.getCdnaCodingEnd() - cdnaCodingStart) % 3;
            Boolean stopToSolve = true;
            if(!splicing && cdnaVariantStart != -1) {  // just checks cdnaVariantStart!=null because no splicing means cdnaVariantEnd is also != null
                codingAnnotationAdded = true;
                if (variant.getReference().length() % 3 == 0) {
                    SoNames.add(VariantAnnotationUtils.INFRAME_DELETION);
                } else {
                    SoNames.add(VariantAnnotationUtils.FRAMESHIFT_VARIANT);
                }
                stopToSolve = false;  // Stop codon annotation will be solved in the line below.
                solveStopCodonNegativeDeletion(transcriptSequence, cdnaCodingStart, cdnaVariantStart, cdnaVariantEnd);
            }
            if (cdnaVariantEnd >= (transcript.getCdnaCodingEnd() - finalNtPhase)) {
                if (transcript.unconfirmedEnd() && (finalNtPhase != 2)) {
                    SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
                } else if(stopToSolve) {  // Only if stop codon annotation was not already solved in the if block above
                    SoNames.add(VariantAnnotationUtils.STOP_LOST);
                }
            }
        }
        if(!codingAnnotationAdded) {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }
    }

    private void solveStopCodonNegativeDeletion(String transcriptSequence, int cdnaCodingStart,
                                                int cdnaVariantStart, int cdnaVariantEnd) {
        Integer variantPhaseShift1 = (cdnaVariantStart - cdnaCodingStart) % 3;
        Integer variantPhaseShift2 = (cdnaVariantEnd - cdnaCodingStart) % 3;
        int modifiedCodon1Start = cdnaVariantStart - variantPhaseShift1;
        int modifiedCodon2Start = cdnaVariantEnd - variantPhaseShift2;
        String reverseCodon1 = new StringBuilder(transcriptSequence.substring(transcriptSequence.length() - modifiedCodon1Start - 2,
                transcriptSequence.length() - modifiedCodon1Start + 1)).reverse().toString(); // Rigth limit of the substring sums +1 because substring does not include that position
        String reverseCodon2 = new StringBuilder(transcriptSequence.substring(transcriptSequence.length() - modifiedCodon2Start - 2,
                transcriptSequence.length() - modifiedCodon2Start + 1)).reverse().toString(); // Rigth limit of the substring sums +1 because substring does not include that position
        String reverseTranscriptSequence = new StringBuilder(transcriptSequence.substring(((transcriptSequence.length()-cdnaVariantEnd)>2)?(transcriptSequence.length()-cdnaVariantEnd-3):0,  // Be careful reaching the end of the transcript sequence
                transcriptSequence.length() - cdnaVariantEnd)).reverse().toString(); // Rigth limit of the substring -2 because substring does not include that position
        char[] referenceCodon1Array = reverseCodon1.toCharArray();
        referenceCodon1Array[0] = VariantAnnotationUtils.complementaryNt.get(referenceCodon1Array[0]);
        referenceCodon1Array[1] = VariantAnnotationUtils.complementaryNt.get(referenceCodon1Array[1]);
        referenceCodon1Array[2] = VariantAnnotationUtils.complementaryNt.get(referenceCodon1Array[2]);
        char[] referenceCodon2Array = reverseCodon2.toCharArray();
        referenceCodon2Array[0] = VariantAnnotationUtils.complementaryNt.get(referenceCodon2Array[0]);
        referenceCodon2Array[1] = VariantAnnotationUtils.complementaryNt.get(referenceCodon2Array[1]);
        referenceCodon2Array[2] = VariantAnnotationUtils.complementaryNt.get(referenceCodon2Array[2]);
        char[] modifiedCodonArray = referenceCodon1Array.clone();

        int i=0;
        int codonPosition;
        for(codonPosition=variantPhaseShift1; codonPosition<3; codonPosition++) { // BE CAREFUL: this method is assumed to be called after checking that cdnaVariantStart and cdnaVariantEnd are within coding sequence (both of them within an exon).
            if(i>=reverseTranscriptSequence.length()) {
                int genomicCoordinate = transcript.getStart()-(i-reverseTranscriptSequence.length()+1);
                modifiedCodonArray[codonPosition] = VariantAnnotationUtils.complementaryNt.get(((GenomeSequenceFeature) genomeDBAdaptor.getSequenceByRegion(variant.getChromosome(),
                        genomicCoordinate, genomicCoordinate + 1, new QueryOptions()).getResult().get(0)).getSequence().charAt(0));
            } else {
                modifiedCodonArray[codonPosition] = VariantAnnotationUtils.complementaryNt.get(reverseTranscriptSequence.charAt(i));  // Paste reference nts after deletion in the corresponding codon position
            }
            i++;
        }

        decideStopCodonModificationAnnotation(SoNames, VariantAnnotationUtils.isStopCodon(String.valueOf(referenceCodon2Array)) ? String.valueOf(referenceCodon2Array) : String.valueOf(referenceCodon1Array),
                modifiedCodonArray);
    }

    private void solveTranscriptFlankingRegions(String leftRegionTag, String rightRegionTag) {
        // Variant overlaps with -5kb region
        if(regionsOverlap(transcript.getStart()-5000, transcript.getStart()-1, variantStart, variantEnd)) {
            // Variant overlaps with -2kb region
            if(regionsOverlap(transcript.getStart()-2000, transcript.getStart()-1, variantStart, variantEnd)) {
                SoNames.add("2KB_" + leftRegionTag);
            } else {
                SoNames.add(leftRegionTag);
            }
        }
        // Variant overlaps with +5kb region
        if(regionsOverlap(transcript.getEnd()+1, transcript.getEnd()+5000, variantStart, variantEnd)) {
            // Variant overlaps with +2kb region
            if(regionsOverlap(transcript.getEnd()+1, transcript.getEnd()+2000, variantStart, variantEnd)) {
                SoNames.add("2KB_" + rightRegionTag);
            } else {
                SoNames.add(rightRegionTag);
            }
        }
    }

    private void solveCodingPositiveTranscript() {
        Exon exon = transcript.getExons().get(0);
        String transcriptSequence = exon.getSequence();
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;

        if (transcript.getGenomicCodingStart() <= exon.getEnd()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variantStart >= exon.getStart()) {
            if (variantStart <= exon.getEnd()) {  // Variant start within the exon
                cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                consequenceType.setcDnaPosition(cdnaVariantStart);
                if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                }
            }
        } else if (variantEnd <= exon.getEnd()) {
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
        } // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon


        int exonCounter = 1;
        while (exonCounter < transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getEnd() + 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            transcriptSequence = transcriptSequence + exon.getSequence();
            if (firstCdsPhase == -1 && transcript.getGenomicCodingStart() <= exon.getEnd()) {  // Set firsCdsPhase only when the first coding exon is reached
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(prevSpliceSite, exon.getStart() - 1, VariantAnnotationUtils.SPLICE_DONOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if (variantStart >= exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantStart <= exon.getEnd()) {  // Variant start within the exon
                    cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                    consequenceType.setcDnaPosition(cdnaVariantStart);
                    if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                        cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                    }
                }
            } else if (variantEnd <= exon.getEnd()) {
                if (variantEnd >= exon.getStart()) {  // Only variant end within the exon  ----||||||||||E||||----
                    cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                } else {  // Variant does not include this exon, variant is located before this exon
                    variantAhead = false;
                }
            } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
            }
            exonCounter++;
        }
        // Is not intron variant (both ends fall within the same intron)
        if (!junctionSolution[1]) {
            solveExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaVariantStart, cdnaVariantEnd,
                    firstCdsPhase);
        }

    }

    private void solveNonCodingPositiveTranscript() {
        Exon exon = transcript.getExons().get(0);
        boolean variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        int cdnaExonEnd = (exon.getEnd() - exon.getStart() + 1);
        int cdnaVariantStart = -1;
        int cdnaVariantEnd = -1;
        int firstCdsPhase = -1;
        boolean[] junctionSolution = {false, false};
        boolean splicing = false;

        if (transcript.getGenomicCodingStart() <= exon.getEnd()) {
            firstCdsPhase = exon.getPhase();
        }
        if (variantStart >= exon.getStart()) {
            if (variantStart <= exon.getEnd()) {  // Variant start within the exon
                cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                consequenceType.setcDnaPosition(cdnaVariantStart);
                if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                }
            }
        } else if (variantEnd <= exon.getEnd()) {
            // We do not contemplate that variant end can be located before this exon since this is the first exon
            cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
        } // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon


        int exonCounter = 1;
        while (exonCounter < transcript.getExons().size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            int prevSpliceSite = exon.getEnd() + 1;
            exon = transcript.getExons().get(exonCounter);          // next exon has been loaded
            if (firstCdsPhase == -1 && transcript.getGenomicCodingStart() <= exon.getEnd()) {  // Set firsCdsPhase only when the first coding exon is reached
                firstCdsPhase = exon.getPhase();
            }
            solveJunction(prevSpliceSite, exon.getStart() - 1, VariantAnnotationUtils.SPLICE_DONOR_VARIANT,
                    VariantAnnotationUtils.SPLICE_ACCEPTOR_VARIANT, junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if (variantStart >= exon.getStart()) {
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                if (variantStart <= exon.getEnd()) {  // Variant start within the exon
                    cdnaVariantStart = cdnaExonEnd - (exon.getEnd() - variantStart);
                    consequenceType.setcDnaPosition(cdnaVariantStart);
                    if (variantEnd <= exon.getEnd()) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                        cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                    }
                }
            } else if (variantEnd <= exon.getEnd()) {
                if (variantEnd >= exon.getStart()) {  // Only variant end within the exon  ----||||||||||E||||----
                    cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
                    cdnaVariantEnd = cdnaExonEnd - (exon.getEnd() - variantEnd);
                } else {  // Variant does not include this exon, variant is located before this exon
                    variantAhead = false;
                }
            } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                cdnaExonEnd += (exon.getEnd() - exon.getStart() + 1);
            }
            exonCounter++;
        }
        solveMiRNA(cdnaVariantStart, cdnaVariantEnd, junctionSolution[1]);
    }

    private void solveExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence,
                                                      int cdnaVariantStart, int cdnaVariantEnd, int firstCdsPhase) {
        if(variantStart<transcript.getGenomicCodingStart()) {
            if(transcript.getStart()<transcript.getGenomicCodingStart() || transcript.unconfirmedStart()) {// Check transcript has 3 UTR
                SoNames.add(VariantAnnotationUtils.FIVE_PRIME_UTR_VARIANT);
            }
            if(variantEnd >= transcript.getGenomicCodingStart()) {
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
                if(transcript.getCdnaCodingStart()>0 || !transcript.unconfirmedStart()) {  // cdnaCodingStart<1 if cds_start_NF and phase!=0
                    SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);
                }
                if(variantEnd>(transcript.getGenomicCodingEnd()-3)) {
                    SoNames.add(VariantAnnotationUtils.STOP_LOST);
                    if (variantEnd > transcript.getGenomicCodingEnd()) {
                        if (transcript.getEnd() > transcript.getGenomicCodingEnd() || transcript.unconfirmedStart()) {// Check transcript has 3 UTR)
                            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
                        }
                    }
                }
            }
        } else if(variantStart <= transcript.getGenomicCodingEnd()) {  // Variant start within coding region
            int cdnaCodingStart = transcript.getCdnaCodingStart();  // Need to define a local cdnaCodingStart because may modified in two lines below
            if(cdnaVariantStart!=-1) {  // cdnaVariantStart may be null if variantStart falls in an intron
                if(transcript.unconfirmedStart()) {
                    cdnaCodingStart -= ((3-firstCdsPhase)%3);
                }
                int cdsVariantStart = cdnaVariantStart - cdnaCodingStart + 1;
                consequenceType.setCdsPosition(cdsVariantStart);
                // First place where protein variant annotation is added to the Consequence type, must create the ProteinVariantAnnotation object
                ProteinVariantAnnotation proteinVariantAnnotation = new ProteinVariantAnnotation();
                proteinVariantAnnotation.setPosition(((cdsVariantStart - 1)/3)+1);
                consequenceType.setProteinVariantAnnotation(proteinVariantAnnotation);
            }
            if(variantEnd <= transcript.getGenomicCodingEnd()) {  // Variant end also within coding region
                solveCodingExonVariantInPositiveTranscript(splicing, transcriptSequence, cdnaCodingStart,
                        cdnaVariantStart, cdnaVariantEnd);
            } else {
                if(transcript.getEnd()>transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
                    SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
                }
                SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
                SoNames.add(VariantAnnotationUtils.STOP_LOST);
            }
        } else if(transcript.getEnd()>transcript.getGenomicCodingEnd() || transcript.unconfirmedEnd()) {// Check transcript has 3 UTR)
            SoNames.add(VariantAnnotationUtils.THREE_PRIME_UTR_VARIANT);
        }
    }

    private void solveCodingExonVariantInPositiveTranscript(boolean splicing, String transcriptSequence, int cdnaCodingStart,
                                                            int cdnaVariantStart, int cdnaVariantEnd) {
        Boolean codingAnnotationAdded = false;  // This will indicate wether it is needed to add the "coding_sequence_variant" annotation or not

        if(cdnaVariantStart != -1 && cdnaVariantStart<(cdnaCodingStart+3) && (cdnaCodingStart>0 || !transcript.unconfirmedStart())) {  // cdnaVariantStart=null if variant is intronic. cdnaCodingStart<1 if cds_start_NF and phase!=0
            SoNames.add(VariantAnnotationUtils.INITIATOR_CODON_VARIANT);
            codingAnnotationAdded = true;
        }
        if(cdnaVariantEnd!=-1) {
            int finalNtPhase = (transcript.getCdnaCodingEnd() - cdnaCodingStart) % 3;
            Boolean stopToSolve = true;
            if(!splicing && cdnaVariantStart != -1) {  // just checks cdnaVariantStart!=null because no splicing means cdnaVariantEnd is also != null
                codingAnnotationAdded = true;
                if (variant.getReference().length() % 3 == 0) {
                    SoNames.add(VariantAnnotationUtils.INFRAME_DELETION);
                } else {
                    SoNames.add(VariantAnnotationUtils.FRAMESHIFT_VARIANT);
                }
                stopToSolve = false;  // Stop codon annotation will be solved in the line below.
                solveStopCodonPositiveDeletion(transcriptSequence, cdnaCodingStart, cdnaVariantStart, cdnaVariantEnd);
            }
            if (cdnaVariantEnd >= (transcript.getCdnaCodingEnd() - finalNtPhase)) {
                if (transcript.unconfirmedEnd() && (finalNtPhase != 2)) {
                    SoNames.add(VariantAnnotationUtils.INCOMPLETE_TERMINAL_CODON_VARIANT);
                } else if(stopToSolve) {  // Only if stop codon annotation was not already solved in the if block above
                    SoNames.add(VariantAnnotationUtils.STOP_LOST);
                }
            }
        }
        if(!codingAnnotationAdded) {
            SoNames.add(VariantAnnotationUtils.CODING_SEQUENCE_VARIANT);
        }
    }

    private void solveStopCodonPositiveDeletion(String transcriptSequence, int cdnaCodingStart, int cdnaVariantStart,
                                                int cdnaVariantEnd) {
        Integer variantPhaseShift1 = (cdnaVariantStart - cdnaCodingStart) % 3;
        Integer variantPhaseShift2 = (cdnaVariantEnd - cdnaCodingStart) % 3;
        int modifiedCodon1Start = cdnaVariantStart - variantPhaseShift1;
        int modifiedCodon2Start = cdnaVariantEnd - variantPhaseShift2;
        String referenceCodon1 = transcriptSequence.substring(modifiedCodon1Start - 1, modifiedCodon1Start + 2);  // -1 and +2 because of base 0 String indexing
        String referenceCodon2 = transcriptSequence.substring(modifiedCodon2Start - 1, modifiedCodon2Start + 2);  // -1 and +2 because of base 0 String indexing
        char[] modifiedCodonArray = referenceCodon1.toCharArray();
        int i=cdnaVariantEnd;  // Position (0 based index) in transcriptSequence of the first nt after the deletion
        int codonPosition;
        for(codonPosition=variantPhaseShift1; codonPosition<3; codonPosition++) { // BE CAREFUL: this method is assumed to be called after checking that cdnaVariantStart and cdnaVariantEnd are within coding sequence (both of them within an exon).
            if(i>=transcriptSequence.length()) {
                int genomicCoordinate = transcript.getEnd()+(i-transcriptSequence.length())+1;
                modifiedCodonArray[codonPosition] = ((GenomeSequenceFeature) genomeDBAdaptor.getSequenceByRegion(variant.getChromosome(),
                        genomicCoordinate, genomicCoordinate+1, new QueryOptions()).getResult().get(0)).getSequence().charAt(0);
            } else {
                modifiedCodonArray[codonPosition] = transcriptSequence.charAt(i);  // Paste reference nts after deletion in the corresponding codon position
            }
            i++;
        }
        decideStopCodonModificationAnnotation(SoNames, VariantAnnotationUtils.isStopCodon(referenceCodon2) ? referenceCodon2 : referenceCodon1, modifiedCodonArray);
    }

    private void solveJunction(Integer spliceSite1, Integer spliceSite2, String leftSpliceSiteTag,
                               String rightSpliceSiteTag, boolean[] junctionSolution) {

        junctionSolution[0] = false;  // Is splicing variant in non-coding region
        junctionSolution[1] = false;  // Variant is intronic and both ends fall within the intron

        if(regionsOverlap(spliceSite1+2, spliceSite2-2, variantStart, variantEnd)) {  // Variant overlaps the rest of intronic region (splice region within the intron and/or rest of intron)
            SoNames.add(VariantAnnotationUtils.INTRON_VARIANT);
        }
        if(variantStart>=spliceSite1 && variantEnd<=spliceSite2) {
            junctionSolution[1] = true;  // variant start & end fall within the intron
        }

        if(regionsOverlap(spliceSite1, spliceSite1 + 1, variantStart, variantEnd)) {  // Variant donor/acceptor
            if((variantEnd-variantStart)<=bigVariantSizeThreshold) {  // Big deletions should not be annotated with such a detail
                SoNames.add(leftSpliceSiteTag);  // donor/acceptor depending on transcript strand
                junctionSolution[0] = (variantStart<=spliceSite2 || variantEnd<=spliceSite2);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            } else {
                junctionSolution[0] = (variantStart<=spliceSite2 || variantEnd<=spliceSite2);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            }
        } else if(regionsOverlap(spliceSite1+2, spliceSite1+7, variantStart, variantEnd)) {
            if((variantEnd-variantStart)<=bigVariantSizeThreshold) {  // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
            }
            junctionSolution[0] = (variantStart<=spliceSite2 || variantEnd<=spliceSite2);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
        } else if(regionsOverlap(spliceSite1-3, spliceSite1-1, variantStart, variantEnd) &&
                    ((variantEnd-variantStart)<=bigVariantSizeThreshold)) {  // Insertion coordinates are passed to this function as (variantStart-1,variantStart)
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
        }

        if(regionsOverlap(spliceSite2-1, spliceSite2, variantStart, variantEnd)) {  // Variant donor/acceptor
            if((variantEnd-variantStart)<=bigVariantSizeThreshold) {  // Big deletions should not be annotated with such a detail
                SoNames.add(rightSpliceSiteTag);  // donor/acceptor depending on transcript strand
                junctionSolution[0] = (spliceSite1<=variantStart || spliceSite1<=variantEnd);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            } else {
                junctionSolution[0] = (spliceSite1<=variantStart || spliceSite1<=variantEnd);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
            }
        } else if(regionsOverlap(spliceSite2-7, spliceSite2-2, variantStart, variantEnd)) {
            if((variantEnd-variantStart)<=bigVariantSizeThreshold) {  // Insertion coordinates are passed to this function as (variantStart-1,variantStart) {
                SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
            }
            junctionSolution[0] = (spliceSite1<=variantStart || spliceSite1<=variantEnd);  //  BE CAREFUL: there are introns shorter than 7nts, and even just 1nt long!! (22:36587846)
        } else if(regionsOverlap(spliceSite2+1, spliceSite2+3, variantStart, variantEnd) &&
                    ((variantEnd-variantStart)<=bigVariantSizeThreshold)) {  // Insertion coordinates are passed to this function as (variantStart-1,variantStart) {
            SoNames.add(VariantAnnotationUtils.SPLICE_REGION_VARIANT);
        }
    }

}


