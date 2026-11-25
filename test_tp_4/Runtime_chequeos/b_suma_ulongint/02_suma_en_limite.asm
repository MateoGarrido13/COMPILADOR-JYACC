.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    x_main DD ?
    y_main DD ?
    r_main DD ?
    c_4294967294_ul_ul DD 4294967294
    c_1_ul_ul DD 1
    @aux4 DD ?
    fmt_ulongint DB "%u",0
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
    jmp main_inicio
main_inicio:
    mov eax, c_4294967294_ul_ul
    mov x_main, eax
    mov eax, c_1_ul_ul
    mov y_main, eax
    mov eax, x_main
    add eax, y_main
    jc error_overflow_suma
    mov @aux4, eax
    mov eax, @aux4
    mov r_main, eax
    invoke crt_printf, offset fmt_ulongint, r_main
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
