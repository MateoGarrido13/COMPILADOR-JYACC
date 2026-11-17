.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    r_main DD ?
    c_2_ul_ul DD 2
    c_3_ul_ul DD 3
    c_1_ul_ul DD 1
    h_main DD ? ; this (layout en 4d)
    @aux11 DD ?
    fmt_ulongint DB "%u",0

.code
start:
    ; (PROGRAMA, --, --)
    ; (CLASE, padre, --)
mt_m_main_padre:
    push ebp
    mov ebp, esp
    ; this: dword ptr [ebp+8]
    mov eax, dword ptr [ebp+12]
    leave
    ret
fin_m_main_padre:
    leave
    ret
    ; (FIN_CLASE, padre, --)
    ; (CLASE, hija, --)
    ; (FIN_CLASE, hija, --)
    ; (PAR, pa:main:padre:m, 2$ul)
    ; (PAR, pb:main:padre:m, 3$ul)
    sub esp, 12
    mov eax, offset h_main
    mov dword ptr [esp], eax
    mov eax, c_2_ul_ul
    mov dword ptr [esp+4], eax
    mov eax, c_3_ul_ul
    mov dword ptr [esp+8], eax
    call mt_m_main_padre
    add esp, 12
    mov @aux11, eax
    mov eax, @aux11
    mov r_main, eax
    invoke crt_printf, offset fmt_ulongint, r_main
    ; (FIN_PROGRAMA, --, --)
    invoke ExitProcess, 0
end start
