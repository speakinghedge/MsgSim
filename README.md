MsgSim
======

processing tool for time based message flow simulation
developed with processing 2.0x

to get an idea of how it works, see the samples in data/

this software is free. use it in any way on your own risk.

change log
----------

25.04.2013

- fixed wrong direction of message if path direction has inverted definition
- moved message position calculation to class Path
- added bezier-based "curved" path (and a little demo -> arc_path.xml)
- updated xsd (added new path type and attribute for the center of the path)

initial version

- scene elements: knot, path, text layer, image layer
- animation: message
- relative/absolute start/end time of message and scene elements







