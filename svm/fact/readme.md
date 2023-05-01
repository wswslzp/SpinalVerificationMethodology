# FACT - A tool for generating UVM testbench

This tool mainly do following steps to achieve the goal of generating a completed UVM testbench. 

1. Extract all top I/O interface of the DUT. 
2. Generate the SV interface for these I/O interfaces. 
3. Generate the driver/monitor/sequencer/sequence item/agent for each interface. 
4. Generate the subscriber for each interface.
5. Generate a golden model stub
6. Generate a general scoreboard that support automately compare the RTL and MOD. 
7. Generate the top ENV which contains all the UVC above and have connection between them.
8. Generate a test base class which contains a ENV. 
9. Generate a top test module named tb_top, which starts the clock/reset signals and invokes the run_test() task.

The I/O interface can be classified as three kinds of protocols in the early stage: '

* None: pure side band signal without any timing behavior.
* Flow: valid only protocol
* Stream: valid-ready protocol

Each protocol has two directions, master and slave, depending on the I/O direction definition. The driver/monitor/sequencer/agent/subscriber has
also two directions. 

## I/O extraction

The Extractor assumes that the top level design has all its I/O defined in `io` bundle. 
Inside the `io`, there may be four kinds of `Data`: `Stream`, `Flow`, `BaseType` and other `Bundle`.
The payload of `Stream` and `Flow` can be either `BaseType` or `Bundle`. These two kinds of bundle has definitive protocol as valid-ready and valid-only.
The `BaseType` is one single bit vector or just a bool wire. These wires each are treated as sideband signals.
The `Bundle` contains `Stream`, `Flow` or `BaseType` (normally) further and the Extractor will extract all the sub-bundles inside just like what it does to `io` bundle.

The output from Extractor is a set of `BundleDef`. Each bundle definition will contain following member:

* protocol: Stream/Flow/Sideband
* payload: a set of Wire
* name: bundle name
* dir: Slave/Master

Utilizing the data structure of `BundleDef`, the interface and corresponding UVC can be generated.
