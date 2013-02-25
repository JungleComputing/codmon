/* $Id: Main.java,v 1.7 2007/06/05 12:54:02 ceriel Exp $ */

import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;

import ibis.io.DataInputStream;
import ibis.io.DataOutputStream;
import ibis.io.BufferedArrayInputStream;
import ibis.io.BufferedArrayOutputStream;
import ibis.io.IbisSerializationInputStream;
import ibis.io.IbisSerializationOutputStream;
import ibis.io.SunSerializationInputStream;
import ibis.io.SunSerializationOutputStream;
import ibis.io.SerializationInput;
import ibis.io.SerializationOutput;

public class Main {

    static final int IBIS = 1;

    static final int SUN = 2;

    static final int SIZE = 10000;

    static final int COUNT = 10000;

    static final int TESTS = 10;

    static boolean reading = false;

    static boolean writing = false;

    static boolean conversion = true;

    static boolean anyconversion = false;

    static boolean sun = false;

    static boolean ibis = false;

    static int tests = TESTS;

    static int count = COUNT;

    static int size = SIZE;

    static int sizeTo = SIZE;

    static String outFile = "";

    static String objType = "";

    public static double round(double val) {
        return (Math.ceil(val * 10.0) / 10.0);
    }

    static void usage() {
        System.err
                .println("Usage: java Main [ -read ] [ -write ] [ -conv ] [ -noconv ] [ -anyconv ]\\");
        System.err.println("              [ -sun ] [ -ibis ] \\");
        System.err.println("              [ -object classname ]* \\");
        System.err.println("              [ -out outputFile ] \\");
        System.err
                .println("              [ -byte | -int | -long | -float | -double ]* \\");
        System.err
                .println("              [ -c <count> ] [ -t <ntests> ] [ -s <size> <sizeTo>]");
    }

    public static void main(String args[]) {
        ArrayList classnames = new ArrayList();
        ArrayList arraytypes = new ArrayList();

        for (int i = 0; i < args.length; i++) {
            if (false) {
            } else if (args[i].equals("-object")) {
                i++;
                if (i == args.length) {
                    usage();
                    System.exit(1);
                }
                classnames.add(args[i]);
		objType = args[i];
            } else if (args[i].equals("-read")) {
                reading = true;
            } else if (args[i].equals("-write")) {
                writing = true;
            } else if (args[i].equals("-conv")) {
                conversion = true;
            } else if (args[i].equals("-noconv")) {
                conversion = false;
	    } else if (args[i].equals("-anyconv")) {
                anyconversion = true;
            } else if (args[i].equals("-byte")) {
                arraytypes.add(new Integer(ArrayContainer.BYTE));
		objType = "byte";
            } else if (args[i].equals("-int")) {
                arraytypes.add(new Integer(ArrayContainer.INT));
		objType = "int";
            } else if (args[i].equals("-long")) {
                arraytypes.add(new Integer(ArrayContainer.LONG));
		objType = "long";
            } else if (args[i].equals("-float")) {
                arraytypes.add(new Integer(ArrayContainer.FLOAT));
		objType = "float";
            } else if (args[i].equals("-double")) {
                arraytypes.add(new Integer(ArrayContainer.DOUBLE));
		objType = "double";
            } else if (args[i].equals("-sun")) {
                sun = true;
            } else if (args[i].equals("-ibis")) {
                ibis = true;
	    } else if (args[i].equals("-out")) { 
		i++;
                if (i == args.length) {
                    usage();
                    System.exit(1);
                }
		outFile = args[i];
            } else if (args[i].equals("-c")) {
                i++;
                if (i == args.length) {
                    usage();
                    System.exit(1);
                }
                count = Integer.parseInt(args[i]);
            } else if (args[i].equals("-t")) {
                i++;
                if (i == args.length) {
                    usage();
                    System.exit(1);
                }
                tests = Integer.parseInt(args[i]);
            } else if (args[i].equals("-s")) {
                i++;
                if (i == args.length) {
                    usage();
                    System.exit(1);
                }
                size = Integer.parseInt(args[i]);
		i++;
                if (i == args.length) {
                    usage();
                    System.exit(1);
                }
		sizeTo = Integer.parseInt(args[i]);
            } else {
                usage();
                System.exit(1);
            }
        }


        if (!conversion && sun) {
            System.err
                    .println("Sun serialization cannot be measured with no conversion");
            System.exit(1);
        }

        for (int i = 0; i < classnames.size(); i++) {
            String classname = (String) classnames.get(i);
            if (sun)
                run_class(classname, SUN);
            if (ibis)
                run_class(classname, IBIS);
        }

	for (int j = size; j <= sizeTo; j = 4*j) {
	    for (int i = 0; i < arraytypes.size(); i++) {
		int type = ((Integer) arraytypes.get(i)).intValue();
		TestObject obj = new ArrayContainer(type, j);
		
		if (sun) {
		    if (reading)
			test_read(obj, SUN, j, conversion);
		    if (writing)
			test_write(obj, SUN, j, conversion);
		}
		if (ibis) {
		    if (reading) {
			test_read(obj, IBIS, j, conversion);
			if (anyconversion)
			    test_read(obj, IBIS, j, !conversion);
		    }
		    if (writing) {
			test_write(obj, IBIS, j, conversion);
			if (anyconversion) {
			    test_write(obj, IBIS, j, !conversion);
			}
		    }
		}
	    }
	}
    }

    static void run_class(String classname, int ser) {
        Class classdef = null;
        TestObject obj = null;

        try {
            classdef = Class.forName(classname);
        } catch (Exception e) {
            System.err.println("Could not load class " + classname);
            System.exit(1);
        }

        try {
            obj = (TestObject) classdef.newInstance();
        } catch (ClassCastException e) {
            System.err.println("Class " + classdef
                    + " is not an instance of TestObject");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Could not instantiate " + classdef);
            System.exit(1);
        }

        if (reading) {
            test_read(obj, ser, 0, conversion);
	    if (ser == IBIS && anyconversion) test_read(obj, ser, 0, !conversion);
        }
        if (writing) {
            test_write(obj, ser, 0, conversion);
	    if (ser == IBIS && anyconversion) test_write(obj, ser, 0, !conversion);
        }
    }

    static void test_read(TestObject obj, int ser, int size, boolean conversion) {
        Object array = null;
        if (obj instanceof ArrayContainer) {
            array = ((ArrayContainer) obj).array;
        }
        try {
	    java.io.FileWriter outF = new java.io.FileWriter(outFile, true);
            long start, end;
            long bytes;

            double best_rtp = 0.0, best_ktp = 0.0;
            long best_time = 1000000;

            StoreBuffer buf = new StoreBuffer();

            DataOutputStream out = null;
            DataInputStream in = null;
            StoreArrayInputStream sin = null;
            SerializationOutput mout = null;
            SerializationInput min = null;

            StoreOutputStream store_out = null;
            StoreInputStream store_in = null;

            if (conversion) {

                store_out = new StoreOutputStream(buf);
                store_in = new StoreInputStream(buf);

                if (ser == SUN) {
                    mout = new SunSerializationOutputStream(store_out);
                    min = new SunSerializationInputStream(store_in);
                    System.err
                            .println("Running SUN serialization read test of "
                                    + obj.id());
                } else {
                    out = new BufferedArrayOutputStream(store_out, 4096);
                    in = new BufferedArrayInputStream(store_in, 4096);
                    mout = new IbisSerializationOutputStream(out);
                    min = new IbisSerializationInputStream(in);
                    System.err
                            .println("Running Ibis serialization read test of "
                                    + obj.id() + " with conversion");
                }
            } else {
                out = new StoreArrayOutputStream(buf);
                in = new StoreArrayInputStream(buf);
                sin = (StoreArrayInputStream) in;
                mout = new IbisSerializationOutputStream(out);
                min = new IbisSerializationInputStream(in);
                System.err.println("Running Ibis serialization read test of "
                        + obj.id() + " without conversion");
            }

            if (array != null) {
                mout.writeObject(array);
            } else {
                mout.writeObject(obj);
            }
            mout.flush();

            min.readObject();

            if (store_in != null)
                store_in.reset();
            if (store_out != null)
                store_out.getAndReset();

            if (sin != null)
                sin.reset();

            buf.clear();

            mout.reset();
            if (array != null) {
                mout.writeObject(array);
            } else {
                mout.writeObject(obj);
            }
            mout.flush();

            bytes = buf.bytesWritten();
            buf.resetBytesWritten();

            if (store_in != null)
                store_in.reset();
            if (store_out != null)
                store_out.getAndReset();

            System.out.println("Wrote " + bytes + " bytes");

            System.out.println("Starting test");

            for (int j = 0; j < tests; j++) {

                start = System.currentTimeMillis();

                for (int i = 0; i < count; i++) {
                    min.readObject();
                    if (sin != null)
                        sin.reset();
                    if (store_in != null)
                        store_in.reset();
                }

                end = System.currentTimeMillis();

                long time = end - start;
                double rb = count* bytes;
                double kb = count * obj.payload();

                double rtp = ((1000.0 * rb) / (1024 * 1024)) / time;
                double ktp = ((1000.0 * kb) / (1024 * 1024)) / time;

                System.out.println("Read took " + time + " ms.  => "
                        + ((1000.0 * time) / (count * obj.num_objs()))
                        + " us/object");
                // System.out.println("Payload bytes read " + kb + " throughput = " + round(ktp) + " MBytes/s");
                // System.out.println("Real bytes read " + rb + " throughput = " + round(rtp) + " MBytes/s");

                if (time < best_time) {
                    best_time = time;
                    best_rtp = rtp;
                    best_ktp = ktp;
                }
            }

            System.out.println("Best result : " + best_rtp + " MBytes/sec ("
                    + round(best_ktp) + " MBytes/sec)");
	    outF.write("<test id=\"s"+size+"r");
	    if (ser == IBIS) {outF.write("ibis");} else {outF.write("sun");}
	    if (!conversion) {outF.write("noconv");} else {outF.write("conv");}
	    outF.write("\" name=\"-read ");
	    if (!conversion) {outF.write("-noconv ");} else {outF.write("-conv ");}
	    if (ser == IBIS) {outF.write("-ibis ");} else {outF.write("-sun ");}
	    outF.write("-"+objType+" -c "+count+" -t "+tests+" -s "+size+"\" value=\""+best_rtp*1024*1024+"\" unit=\"Bytes/s\"/>\n");
	    outF.close();
            System.out.println("" + round(best_rtp) + " " + round(best_ktp));
        } catch (Exception e) {
            System.err.println("Got exception " + e);
            e.printStackTrace();
        }
    }

    static void test_write(TestObject obj, int ser, int size, boolean conversion) {
        Object array = null;
        if (obj instanceof ArrayContainer) {
            array = ((ArrayContainer) obj).array;
        }
        try {
	    java.io.FileWriter outF = new java.io.FileWriter(outFile, true);
            long start, end;
            long bytes;

            double best_rtp = 0.0, best_ktp = 0.0;
            long best_time = 1000000;

            DataOutputStream out = null;
            NullOutputStream os = null;
            SerializationOutput mout = null;

            if (conversion) {
                os = new NullOutputStream();
                if (ser == SUN) {
                    mout = new SunSerializationOutputStream(os);
                    System.err
                            .println("Running Sun serialization write test of "
                                    + obj.id());
                } else {
                    out = new BufferedArrayOutputStream(os, 4096);
                    System.err
                            .println("Running Ibis serialization write test of "
                                    + obj.id() + " with conversion");
                    mout = new IbisSerializationOutputStream(out);
                }
            } else {
                out = new NullArrayOutputStream();
                mout = new IbisSerializationOutputStream(out);
                System.err.println("Running Ibis serialization write test of "
                        + obj.id() + " without conversion");
            }

            System.err.println("Starting test");

            for (int j = 0; j < tests; j++) {

                start = System.currentTimeMillis();

                for (int i = 0; i < count; i++) {
                    if (array != null) {
                        mout.writeObject(array);
                    } else {
                        mout.writeObject(obj);
                    }
                    mout.flush();
                    mout.reset();
                }

                end = System.currentTimeMillis();

                long time = end - start;
                if (os != null) {
                    bytes = os.getAndReset();
                } else {
                    bytes = out.bytesWritten();
                    out.resetBytesWritten();
                }
                double rb = bytes;
                double kb = count * obj.payload();

                double rtp = ((1000.0 * rb) / (1024 * 1024)) / time;
                double ktp = ((1000.0 * kb) / (1024 * 1024)) / time;

                System.out.println("Write took " + time + " ms.  => "
                        + ((1000.0 * time) / (count * obj.num_objs()))
                        + " us/object");
                // System.out.println("Payload bytes written " + kb + " throughput = " + round(ktp) + " MBytes/s");
                // System.out.println("Real bytes written " + rb + " throughput = " + round(rtp) + " MBytes/s");

                if (time < best_time) {
                    best_time = time;
                    best_rtp = rtp;
                    best_ktp = ktp;
                }
            }

            System.out.println("Best result : " + best_rtp + " MBytes/sec ("
                    + round(best_ktp) + " MBytes/sec)");
            System.out.println("" + round(best_rtp) + " " + round(best_ktp));
	    outF.write("<test id=\"s"+size+"w");
	    if (ser == IBIS) {outF.write("ibis");} else {outF.write("sun");}
	    if (!conversion) {outF.write("noconv");} else {outF.write("conv");}
	    outF.write("\" name=\"-write ");
	    if (!conversion) {outF.write("-noconv ");} else {outF.write("-conv ");}
	    if (ser == IBIS) {outF.write("-ibis ");} else {outF.write("-sun ");}
	    outF.write("-"+objType+" -c "+count+" -t "+tests+" -s "+size+"\" value=\""+best_rtp*1024*1024+"\" unit=\"Bytes/s\"/>\n");
	    outF.close();
        } catch (Exception e) {
            System.err.println("Got exception " + e);
            e.printStackTrace();
        }
    }
}
