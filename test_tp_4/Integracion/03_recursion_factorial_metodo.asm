.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    c_1_ul_ul DD 1
    r_main DD ?
    m_main DD ?
    c_5_ul_ul DD 5
    c_main DD ? ; this (layout en 4d)
    @aux3 DD ?
    @aux8 DD ?
    @aux10 DD ?
    @aux11 DD ?
    @aux17 DD ?
    @aux22 DD ?
    @aux25 DD ?
    fmt_ulongint DB "%u",0

.code
start:
fn_fact_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    cmp eax, c_1_ul_ul
    setbe al
    movzx eax, al
    mov @aux3, eax
    mov eax, dword ptr [ebp+8]
    cmp eax, c_1_ul_ul
    ja label_1
    mov eax, c_1_ul_ul
    leave
    ret
    jmp label_2
label_1:
    mov eax, dword ptr [ebp+8]
    sub eax, c_1_ul_ul
    mov @aux8, eax
    ; (PAR, n:main:fact, [8])
    sub esp, 4
    mov eax, @aux8
    mov dword ptr [esp+0], eax
    call fn_fact_main
    add esp, 4
    mov @aux10, eax
    mov eax, dword ptr [ebp+8]
    imul eax, @aux10
    mov @aux11, eax
    mov eax, @aux11
    leave
    ret
label_2:
fin_fact_main:
    leave
    ret
mt_escala_main_calculadora:
    push ebp
    mov ebp, esp
    ; this: dword ptr [ebp+8]
    mov eax, dword ptr [ebp+12]
    imul eax, dword ptr [ebp+12]
    mov @aux17, eax
    mov eax, @aux17
    leave
    ret
fin_escala_main_calculadora:
    leave
    ret
    ; (PAR, n:main:fact, 5$ul)
    sub esp, 4
    mov eax, c_5_ul_ul
    mov dword ptr [esp+0], eax
    call fn_fact_main
    add esp, 4
    mov @aux22, eax
    mov eax, @aux22
    mov r_main, eax
    ; (PAR, v:main:calculadora:escala, r:main)
    sub esp, 8
    mov eax, offset c_main
    mov dword ptr [esp], eax
    mov eax, r_main
    mov dword ptr [esp+4], eax
    call mt_escala_main_calculadora
    add esp, 8
    mov @aux25, eax
    mov eax, @aux25
    mov m_main, eax
    invoke crt_printf, offset fmt_ulongint, r_main
    invoke crt_printf, offset fmt_ulongint, m_main
    invoke ExitProcess, 0
end start
