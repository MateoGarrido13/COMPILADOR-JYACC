.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    d_main DQ ?
    c_5_ul_ul DD 5
    c_10_ul_ul DD 10

.code
start:
    invoke ExitProcess, 0
end start
