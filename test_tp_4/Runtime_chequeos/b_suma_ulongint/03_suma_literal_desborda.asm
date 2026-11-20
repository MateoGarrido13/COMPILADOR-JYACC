.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    r_main DD ?
    c_4294967295_ul_ul DD 4294967295
    c_1_ul_ul DD 1
    @aux2 DD ?
    fmt_ulongint DB "%u",0
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
    mov eax, c_4294967295_ul_ul
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux2, eax
    mov eax, @aux2
    mov r_main, eax
    invoke crt_printf, offset fmt_ulongint, r_main
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
