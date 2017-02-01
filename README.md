Map matching is the problem of how to match recorded geographic coordinates to a logical model of the real world, 
typically using some form of Geographic Information System[1].

This project aims to perform map-matching on (initially) 2D data, using Barefoot API[2], which uses OpenStreetMap internally.
For each data tuple (t,x,y) the most appropriate edge ID is returned using the Barefoot API.
where, t is the time or sequence ID.

References:
[1] https://en.wikipedia.org/wiki/Map_matching
[2] https://github.com/bmwcarit/barefoot
