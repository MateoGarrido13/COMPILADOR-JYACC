.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    r_main DD ?
    c_10_ul_ul DD 10
    c_20_ul_ul DD 20
    @aux4 DD ?
    fmt_ulongint DB "%u",0
    msg_negativo_resta DB "Error: resultado negativo en resta de ulongint",0

.code
start:
    mov eax, c_10_ul_ul
    mov x_main, eax
    mov eax, c_20_ul_ul
    mov y_main, eax
    mov eax, x_main
    sub eax, y_main
    jb error_negativo_resta
    mov @aux4, eax
    mov eax, @aux4
    mov r_main, eax
    invoke crt_printf, offset fmt_ulongint, r_main
error_negativo_resta:
    invoke crt_printf, offset msg_negativo_resta
    invoke ExitProcess, 3
    invoke ExitProcess, 0
end start
