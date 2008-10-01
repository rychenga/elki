package de.lmu.ifi.dbs.elki.algorithm.result.outlier;

import de.lmu.ifi.dbs.elki.algorithm.outlier.LOFTable;
import de.lmu.ifi.dbs.elki.algorithm.outlier.NNTable;
import de.lmu.ifi.dbs.elki.algorithm.result.AbstractResult;
import de.lmu.ifi.dbs.elki.data.ClassLabel;
import de.lmu.ifi.dbs.elki.data.DatabaseObject;
import de.lmu.ifi.dbs.elki.database.AssociationID;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.normalization.NonNumericFeaturesException;
import de.lmu.ifi.dbs.elki.normalization.Normalization;
import de.lmu.ifi.dbs.elki.utilities.UnableToComplyException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AttributeSettings;
import de.lmu.ifi.dbs.elki.utilities.pairs.CompareSwappedDescending;
import de.lmu.ifi.dbs.elki.utilities.pairs.IntDoublePair;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides the result of the LOF algorithm.
 *
 * @author Peer Kro&uml;ger
 * @param <O> the type of DatabaseObjects handled by this Result
 */

public class LOFResult<O extends DatabaseObject> extends AbstractResult<O> {

    /**
     * Marker for a file name containing lofs.
     */
    public static final String LOF_MARKER = "lof";

    /**
     * Marker for a file name containing the nearest neighbors.
     */
    public static final String NN_TABLE_MARKER = "nnTable";

    /**
     * Marker for a file name containing the reverse nearest neighbors.
     */
    public static final String RNN_TABLE_MARKER = "rnnTable";

    /**
     * Marker for a file name containing the lofTable.
     */
    public static final String LOF_TABLE_MARKER = "lofTable";

    /**
     * Contains the LOF for each object.
     */
    private LOFTable lofTable;

    /**
     * Contains the nearest and the reverse nearest neighbors for each object.
     */
    private NNTable nnTable;

    /**
     * Standard constructor. Constructs a new LOFResult set from a database and
     * an array of IDs and double values.
     *
     * @param db       the database from which the LOFs have been computed
     * @param lofTable table holding the LOF for each object
     * @param nnTable  table holding the nearest and the reverse nearest
     *                 neighbors for each object
     */
    public LOFResult(Database<O> db, LOFTable lofTable, NNTable nnTable) {
        super(db);
        this.db = db;
        this.lofTable = lofTable;
        this.nnTable = nnTable;
    }

    public void output(File out, Normalization<O> normalization,
                       List<AttributeSettings> settings) throws UnableToComplyException {
        PrintStream outStream;
        try {
            File lofFile = new File(out.getAbsolutePath() + File.separator + LOF_MARKER + FILE_EXTENSION);
            lofFile.getParentFile().mkdirs();
            PrintStream lofOut = new PrintStream(new FileOutputStream(lofFile));
            outputLOF(lofOut, normalization, settings);
            lofOut.flush();

            File lofTableFile = new File(out.getAbsolutePath() + File.separator + LOF_TABLE_MARKER + FILE_EXTENSION);
            PrintStream lofTableOut = new PrintStream(new FileOutputStream(lofTableFile));
            writeHeader(lofTableOut, settings, null);
            lofTable.write(lofTableOut);
            lofTableOut.flush();

            File nnTableFile = new File(out.getAbsolutePath() + File.separator + NN_TABLE_MARKER + FILE_EXTENSION);
            PrintStream nnTableOut = new PrintStream(new FileOutputStream(nnTableFile));
            writeHeader(nnTableOut, settings, null);
            nnTable.write(nnTableOut);
            nnTableOut.flush();
        }
        catch (Exception e) {
            outStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
            output(outStream, normalization, settings);
        }
    }

    public void output(PrintStream outStream, Normalization<O> normalization,
                       List<AttributeSettings> settings) throws UnableToComplyException {

        outputLOF(outStream, normalization, settings);
        outStream.flush();
    }

    /**
     * Writes the lofs to output.
     *
     * @param outStream     the stream to write to
     * @param normalization Normalization to restore original values according to, if this action is supported
     *                      - may remain null.
     * @param settings      the settings to be written into the header, if this parameter is <code>null</code>,
     *                      no header will be written
     * @throws de.lmu.ifi.dbs.elki.utilities.UnableToComplyException
     *          if an error during normalization occurs
     */
    private void outputLOF(PrintStream outStream, Normalization<O> normalization,
                           List<AttributeSettings> settings) throws UnableToComplyException {

        writeHeader(outStream, settings, null);

        try {
            // build lofs
            List<IntDoublePair> lofs = new ArrayList<IntDoublePair>(db.size());
            Iterator<Integer> it = db.iterator();
            while (it.hasNext()) {
                Integer id = it.next();
                double lof = lofTable.getLOFEntry(id).getLOF();
                lofs.add(new IntDoublePair(id, lof));
            }

            // sort lofs
            Collections.sort(lofs, new CompareSwappedDescending<IntDoublePair>());

            // write lofs
            for (IntDoublePair idDoublePair : lofs) {
                double lof = idDoublePair.getSecond();
                int objectID = idDoublePair.getFirst();

                outStream.print("ID=");
                outStream.print(objectID);
                outStream.print(" ");

                O object = db.get(objectID);
                if (normalization != null) {
                    O restored = normalization.restore(object);
                    outStream.print(restored.toString());
                }
                else {
                    outStream.print(object.toString());
                }
                outStream.print(" ");

                String label = db.getAssociation(AssociationID.LABEL, objectID);
                if (label != null) {
                    outStream.print(label);
                    outStream.print(" ");
                }

                ClassLabel classLabel = db.getAssociation(AssociationID.CLASS, objectID);
                if (classLabel != null) {
                    outStream.print(classLabel);
                    outStream.print(" ");
                }

                String externalID = db.getAssociation(AssociationID.EXTERNAL_ID, objectID);
                if (externalID != null) {
                    outStream.print(externalID);
                    outStream.print(" ");
                }
                outStream.print("LOF=");
                outStream.println(lof);
            }
        }
        catch (NonNumericFeaturesException e) {
            throw new UnableToComplyException(e);
        }

        outStream.flush();
    }

}
