.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    r_main DD ?
    c_10_ul_ul DD 10
    c_5_ul_ul DD 5
    c_1_ul_ul DD 1
    c_2_ul_ul DD 2
    c_main DD ? ; this (layout en 4d)
    @aux4 DD ?
    @aux10 DD ?
    fmt_ulongint DB "%u",0
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
mt_m_main_calculadora:
    push ebp
    mov ebp, esp
    ; this: dword ptr [ebp+8]
    mov eax, dword ptr [ebp+12]
    add eax, dword ptr [ebp+16]
    jc error_overflow_suma
    mov @aux4, eax
    mov eax, @aux4
    leave
    ret
fin_m_main_calculadora:
    leave
    ret
    ; (PAR, base:main:calculadora:m, 10$ul)
    ; (PAR, extra:main:calculadora:m, 5$ul)
    sub esp, 12
    mov eax, offset c_main
    mov dword ptr [esp], eax
    mov eax, c_10_ul_ul
    mov dword ptr [esp+4], eax
    mov eax, c_5_ul_ul
    mov dword ptr [esp+8], eax
    call mt_m_main_calculadora
    add esp, 12
    mov @aux10, eax
    mov eax, @aux10
    mov r_main, eax
    invoke crt_printf, offset fmt_ulongint, r_main
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
