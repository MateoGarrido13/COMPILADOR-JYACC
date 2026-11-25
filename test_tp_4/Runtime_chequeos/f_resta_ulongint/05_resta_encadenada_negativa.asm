.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    z_main DD ?
    c_5_ul_ul DD 5
    c_10_ul_ul DD 10
    c_2_ul_ul DD 2
    c_100_ul_ul DD 100
    @aux2 DD ?
    @aux4 DD ?
    @aux5 DD ?
    @aux7 DD ?
    fmt_ulongint DB "%u",0
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0
    msg_negativo_resta DB "Error: resultado negativo en resta de ulongint",0

.code
start:
    jmp main_inicio
main_inicio:
    mov eax, c_5_ul_ul
    sub eax, c_10_ul_ul
    jb error_negativo_resta
    mov @aux2, eax
    mov eax, @aux2
    mov x_main, eax
    mov eax, c_10_ul_ul
    add eax, c_2_ul_ul
    jc error_overflow_suma
    mov @aux4, eax
    mov eax, @aux4
    sub eax, c_100_ul_ul
    jb error_negativo_resta
    mov @aux5, eax
    mov eax, @aux5
    mov y_main, eax
    mov eax, x_main
    add eax, c_5_ul_ul
    jc error_overflow_suma
    mov @aux7, eax
    mov eax, @aux7
    mov z_main, eax
    invoke crt_printf, offset fmt_ulongint, x_main
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
error_negativo_resta:
    invoke crt_printf, offset msg_negativo_resta
    invoke ExitProcess, 3
fin_normal:
    invoke ExitProcess, 0
end start
