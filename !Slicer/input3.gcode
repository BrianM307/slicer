;FLAVOR:Marlin
;TIME:38542
;Filament used: 15.364m
;Layer height: 0.2
;Generated with Cura_SteamEngine 3.6.0
M104 S200
M105
M109 S200
M82 ;absolute extrusion mode
G28 ;Home
G1 Z15.0 F6000 ;Move the platform down 15mm
;Prime the extruder
G92 E0
G1 F200 E3
G92 E0
G92 E0
G1 F1500 E-6.5
;LAYER_COUNT:228
;LAYER:0
M107
G0 F3600 X30 Y30 Z0.3
;TYPE:SKIRT
G1 X30 Y130 E0.02515
G1 X330 Y330 E0.05649
G1 X330 Y30 E0.07133
G1 X30 Y30 E0.09133