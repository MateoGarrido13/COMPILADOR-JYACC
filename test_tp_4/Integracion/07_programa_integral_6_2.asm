.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    u_main_basea DD ?
    v_main_baseb DQ ?
    w_main_derivada DD ?
    c_1_ul_ul DD 1
    s1_basea_u_main DD ?
    s1_baseb_v_main DQ ?
    s1_w_main DD ?
    s2_basea_u_main DD ?
    s2_baseb_v_main DQ ?
    s2_w_main DD ?
    i_main DD ?
    total_main DD ?
    c_4_ul_ul DD 4
    c_1_25 DQ 1.25
    c_9_ul_ul DD 9
    c_0_ul_ul DD 0
    c_5_ul_ul DD 5
    total DB "total=",0
    @aux9 DD ?
    @aux23 DD ?
    @aux25 DD ?
    @aux28 DD ?
    @aux30 DD ?
    @aux37 DD ?
    fmt_ulongint DB "%u",0
    fmt_doublef DB "%f",0
    fmt_cadena DB "%s",0
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
fn_fact_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    cmp eax, c_1_ul_ul
    setbe al
    movzx eax, al
    mov @aux9, eax
    mov eax, dword ptr [ebp+8]
    cmp eax, c_1_ul_ul
    ja label_1
    mov eax, c_1_ul_ul
    leave
    ret
    jmp label_2
label_1:
label_2:
fin_fact_main:
    leave
    ret
    mov eax, c_4_ul_ul
    mov s1_basea_u_main, eax
    fld c_1_25
    fstp s1_baseb_v_main
    mov eax, c_9_ul_ul
    mov s1_w_main, eax
    mov eax, s1_basea_u_main
    mov s2_basea_u_main, eax
    mov eax, dword ptr [s1_baseb_v_main+4]
    mov dword ptr [s2_baseb_v_main+4], eax
    mov eax, dword ptr [s1_baseb_v_main]
    mov dword ptr [s2_baseb_v_main], eax
    mov eax, s1_w_main
    mov s2_w_main, eax
    mov eax, c_1_ul_ul
    mov i_main, eax
    mov eax, c_0_ul_ul
    mov total_main, eax
label_3:
    mov eax, i_main
    cmp eax, c_4_ul_ul
    setbe al
    movzx eax, al
    mov @aux23, eax
    mov eax, i_main
    cmp eax, c_4_ul_ul
    ja label_4
    mov eax, total_main
    add eax, i_main
    jc error_overflow_suma
    mov @aux25, eax
    mov eax, @aux25
    mov total_main, eax
label_4:
    mov eax, i_main
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux28, eax
    mov eax, @aux28
    mov i_main, eax
    mov eax, i_main
    cmp eax, c_5_ul_ul
    setbe al
    movzx eax, al
    mov @aux30, eax
    mov eax, i_main
    cmp eax, c_5_ul_ul
    ja label_5
    jmp label_3
label_5:
    invoke crt_printf, offset fmt_cadena, offset total
    invoke crt_printf, offset fmt_ulongint, total_main
    ; (PAR, n:main:fact, 5$ul)
    sub esp, 4
    mov eax, c_5_ul_ul
    mov dword ptr [esp+0], eax
    call fn_fact_main
    add esp, 4
    mov @aux37, eax
    invoke crt_printf, offset fmt_ulongint, @aux37
    invoke crt_printf, offset fmt_ulongint, s2_basea_u_main
    invoke crt_printf, offset fmt_doublef, s2_baseb_v_main
    invoke crt_printf, offset fmt_ulongint, s2_w_main
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
