.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    c_42_ul_ul DD 42
    c_3_5 DQ 3.5
    fmt_ulongint DB "%u",0
    fmt_doublef DB "%f",0

.code
start:
    ; (PROGRAMA, --, --)
    invoke crt_printf, offset fmt_ulongint, c_42_ul_ul
    invoke crt_printf, offset fmt_doublef, c_3_5
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
